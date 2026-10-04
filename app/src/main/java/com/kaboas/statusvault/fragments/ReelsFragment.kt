package com.kaboas.statusvault.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kaboas.statusvault.PreviewActivity
import com.kaboas.statusvault.R
import com.kaboas.statusvault.adapter.ReelsAdapter
import com.kaboas.statusvault.data.AppDatabase
import com.kaboas.statusvault.data.FavoriteEntity
import com.kaboas.statusvault.data.MediaRepository
import com.kaboas.statusvault.data.MediaType
import com.kaboas.statusvault.utils.DeleteHelper
import com.kaboas.statusvault.utils.DownloadHelper
import kotlinx.coroutines.launch
import java.io.File

class ReelsFragment : Fragment() {

    private var adapter: ReelsAdapter? = null
    private var mediaType = MediaType.VIDEO
    private var isFavorites = false
    private var recycler: RecyclerView? = null
    private var txtEmpty: TextView? = null

    companion object {
        private const val ARG_TYPE = "type"
        private const val ARG_FAVORITES = "favorites"

        fun newInstance(type: MediaType): ReelsFragment {
            val f = ReelsFragment()
            val b = Bundle()
            b.putString(ARG_TYPE, type.name)
            b.putBoolean(ARG_FAVORITES, false)
            f.arguments = b
            return f
        }

        fun newInstanceFavorites(): ReelsFragment {
            val f = ReelsFragment()
            val b = Bundle()
            b.putBoolean(ARG_FAVORITES, true)
            f.arguments = b
            return f
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reels_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isFavorites = arguments?.getBoolean(ARG_FAVORITES) ?: false
        mediaType = MediaType.valueOf(arguments?.getString(ARG_TYPE) ?: MediaType.VIDEO.name)

        recycler = view.findViewById(R.id.recyclerReels)
        txtEmpty = view.findViewById(R.id.txtEmptyReels)

        recycler?.layoutManager = LinearLayoutManager(requireContext())

        if (isFavorites) loadFavorites() else loadMedia()
    }

    override fun onResume() {
        super.onResume()
        MediaRepository.clearCache()
        if (!isFavorites) loadMedia()
    }

    private fun loadMedia() {
        val r = recycler ?: return
        val e = txtEmpty ?: return

        val files = MediaRepository.listMedia(mediaType)

        if (files.isEmpty()) {
            r.visibility = View.GONE
            e.visibility = View.VISIBLE
            adapter = null
            r.adapter = null
            return
        }

        r.visibility = View.VISIBLE
        e.visibility = View.GONE

        if (adapter == null) {
            adapter = ReelsAdapter(
                files.toMutableList(),
                favorites = mutableSetOf(),
                isFavoritesTab = false,
                onDownload = { file -> DownloadHelper.downloadFile(requireContext(), file, mediaType) },
                onFavorite = { file -> toggleFavorite(file) },
                onDelete = { file ->
                    if (DeleteHelper.deleteFile(requireContext(), file)) {
                        adapter?.removeItem(file)
                        if (adapter?.itemCount == 0) {
                            r.visibility = View.GONE
                            e.visibility = View.VISIBLE
                        }
                        Toast.makeText(requireContext(), "🗑️ Deleted", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "❌ Cannot delete", Toast.LENGTH_SHORT).show()
                    }
                },
                onItemClick = { file ->
                    val intent = Intent(requireContext(), PreviewActivity::class.java)
                    intent.putExtra("file_path", file.absolutePath)
                    startActivity(intent)
                }
            )
            r.adapter = adapter
        } else {
            adapter?.updateData(files)
        }

        val dao = AppDatabase.getInstance(requireContext()).favoriteDao()
        dao.getAll().observe(viewLifecycleOwner) { favs ->
            adapter?.setFavorites(favs.map { it.path }.toSet())
        }
    }

    private fun loadFavorites() {
        val r = recycler ?: return
        val e = txtEmpty ?: return

        val dao = AppDatabase.getInstance(requireContext()).favoriteDao()
        dao.getAll().observe(viewLifecycleOwner) { favs ->
            val files = favs.mapNotNull {
                val f = File(it.path)
                if (f.exists()) f else null
            }

            if (files.isEmpty()) {
                r.visibility = View.GONE
                e.visibility = View.VISIBLE
                adapter = null
                r.adapter = null
                return@observe
            }

            r.visibility = View.VISIBLE
            e.visibility = View.GONE

            if (adapter == null) {
                adapter = ReelsAdapter(
                    files.toMutableList(),
                    favorites = files.map { it.absolutePath }.toMutableSet(),
                    isFavoritesTab = true,
                    onDownload = { file -> DownloadHelper.downloadFile(requireContext(), file, mediaType) },
                    onFavorite = { file -> toggleFavorite(file) },
                    onDelete = { file ->
                        if (DeleteHelper.deleteFile(requireContext(), file)) {
                            Toast.makeText(requireContext(), "🗑️ Deleted", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "❌ Cannot delete", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onItemClick = { file ->
                        val intent = Intent(requireContext(), PreviewActivity::class.java)
                        intent.putExtra("file_path", file.absolutePath)
                        startActivity(intent)
                    }
                )
                r.adapter = adapter
            } else {
                adapter?.updateData(files)
            }
        }
    }

    private fun toggleFavorite(file: File) {
        lifecycleScope.launch {
            val dao = AppDatabase.getInstance(requireContext()).favoriteDao()
            val isFav = dao.isFavorite(file.absolutePath)
            if (isFav) {
                dao.deleteByPath(file.absolutePath)
                Toast.makeText(requireContext(), "💔 Removed", Toast.LENGTH_SHORT).show()
            } else {
                dao.insert(FavoriteEntity(file.absolutePath, file.name, file.extension))
                Toast.makeText(requireContext(), "❤️ Added", Toast.LENGTH_SHORT).show()
            }
            adapter?.toggleFavorite(file)
        }
    }
}
