package com.example.notesapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.Switch
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notesapp.Adapter.NotesAdapter
import com.example.notesapp.Database.NoteDatabase
import com.example.notesapp.Models.Note
import com.example.notesapp.Models.NoteViewModel
import com.example.notesapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), NotesAdapter.NotesClickListener, PopupMenu.OnMenuItemClickListener {

    private lateinit var binding : ActivityMainBinding
    private lateinit var database: NoteDatabase
    lateinit var viewModel : NoteViewModel
    lateinit var adapter: NotesAdapter
    lateinit var selectedNote : Note
    private lateinit var switchD : Switch

    private val updateNote = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    {
        result ->
        if(result.resultCode == Activity.RESULT_OK)
        {
            val note = result.data?.getSerializableExtra("note") as? Note
            if(note != null)
            {
                viewModel.updateNote(note)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Initializing the UI
        initUI()

        viewModel = ViewModelProvider(this , ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(NoteViewModel::class.java)

        viewModel.allnotes.observe(this)
        {
            list ->
            list.let{
                adapter.updateList(list)
            }
        }

        switchD = findViewById(R.id.switchD)
        switchD.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_YES
            } else {
                delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_NO
            }
        }


        database = NoteDatabase.getDatabase(this)
    }

    private fun initUI() {

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = StaggeredGridLayoutManager(2, LinearLayout.VERTICAL)
        adapter = NotesAdapter(this, this)
        binding.recyclerView.adapter = adapter

        val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
            if(result.resultCode == Activity.RESULT_OK)
            {
                val note = result.data?.getSerializableExtra("note") as? Note
                if(note != null)
                {
                    viewModel.insertNote(note)
                }
            }
        }

        binding.fbAddNote.setOnClickListener{
            val intent = Intent(this, AddNote::class.java)
            getContent.launch(intent)
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if(newText != null)
                {
                    adapter.filterList(newText)
                }
                return true
            }

        })
    }

    override fun onItemClicked(note: Note) {
        val intent = Intent(this@MainActivity, AddNote::class.java)
        intent.putExtra("current_note",note)
        updateNote.launch(intent)
    }

    override fun onLongItemClicked(note: Note, cardView: CardView) {
        selectedNote = note
        popUpDisplay(cardView)
    }

    private fun popUpDisplay(cardView: CardView)
    {
        val popup = PopupMenu(this, cardView)
        popup.setOnMenuItemClickListener(this@MainActivity)
        popup.inflate(R.menu.pop_up_menus)
        popup.show()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if(item?.itemId == R.id.del)
        {
            viewModel.deleteNote((selectedNote))
            return true
        }
        else if (item?.itemId == R.id.share) {
            val intent = Intent(Intent.ACTION_SEND)
            intent.setType("text/plain")
            var title = selectedNote.title.toString()
            var txt = selectedNote.note.toString()
            intent.putExtra(Intent.EXTRA_TEXT, (title + "\n" + txt))
            startActivity(Intent.createChooser(intent, "Share"))
            return true
        }
        return false
    }
}