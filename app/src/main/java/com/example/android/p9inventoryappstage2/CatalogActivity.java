package com.example.android.p9inventoryappstage2;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.android.p9inventoryappstage2.data.bookContract.bookEntry;

import android.support.design.widget.FloatingActionButton;
import android.widget.Toast;

/**
 * Displays list of books that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * Identifier for the book data loader
     */
    private static final int BOOK_LOADER = 0;
    /**
     * Adapter for the ListView
     */
    BooksCursorAdapter mCursorAdapter;
    /**
     * FAB to open EditorActivity (on add-new-book mode)
     */
    FloatingActionButton addBookButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        // Find the floating action button that will be pressed in order to add a new book to the inventory
        addBookButton = (FloatingActionButton) findViewById(R.id.fab_add_book);
        // Executed when the add book button is clicked
        addBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open the EditorActivity
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
        // Find the ListView which will be populated with the book data
        ListView bookListView = (ListView) findViewById(R.id.list);
        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        bookListView.setEmptyView(emptyView);
        // Setup an Adapter to create a list item for each row of book data in the Cursor.
        // There is no book data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new BooksCursorAdapter(this, null);
        bookListView.setAdapter(mCursorAdapter);
        // Setup the item click listener (Executed when a book item in the list is clicked)
        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                // Form the content URI that represents the specific book that was clicked on,
                // by appending the "id" (passed as input to this method) onto the {@link bookEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.p9inventoryappstage2/books/2"
                // if the book with ID 2 was clicked on.
                Uri currentBookUri = ContentUris.withAppendedId(bookEntry.CONTENT_URI, id);
                // Set the URI on the data field of the intent
                intent.setData(currentBookUri);
                // Launch the {@link EditorActivity} to display the data for the current book.
                startActivity(intent);
            }
        });
        // Kick off the loader
        getLoaderManager().initLoader(BOOK_LOADER, null, this);
    }

    /**
     * Helper method to insert hardcoded dummy book data into the database. For debugging purposes only.
     */
    private void insertBook() {
        // Create a ContentValues object where column names are the keys, and Le Petit Prince book attributes are the values.
        ContentValues values = new ContentValues();
        values.put(bookEntry.COLUMN_BOOK_TITLE, "Le Petit Prince");
        values.put(bookEntry.COLUMN_BOOK_AUTHOR, "Antoine De Saint-Exupery");
        values.put(bookEntry.COLUMN_BOOK_PRICE, 50.9);
        values.put(bookEntry.COLUMN_BOOK_QUANTITY, 7);
        values.put(bookEntry.COLUMN_BOOK_SUPPLIER_NAME, "Stimatzky");
        values.put(bookEntry.COLUMN_BOOK_SUPPLIER_PHONE, "+97239462106");
        // Insert a new row for Le Petit Prince into the provider using the ContentResolver.
        // Use the {@link bookEntry#CONTENT_URI} to indicate that we want to insert into the books database table.
        // Receive the new content URI that will allow us to access Le Petit Prince data in the future.
        Uri newUri = getContentResolver().insert(bookEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all books in the database.
     */
    private void deleteAllBooks() {
        int rowsDeleted = getContentResolver().delete(bookEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from book database");
        if (rowsDeleted == 0) {
            // If not all rows were deleted, then there was an error with the delete.
            Toast.makeText(this, getString(R.string.editor_delete_all_books_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the delete was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.editor_delete_all_books_successful),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertBook();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // If the book inventory is already empty
                if (mCursorAdapter.getCount() == 0)
                    Toast.makeText(this, getText(R.string.book_list_is_empty), Toast.LENGTH_LONG).show();
                    // If the book inventory is not empty
                else if (mCursorAdapter.getCount() != 0)
                    // Pop up confirmation dialog for deletion
                    showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Prompt the user to confirm that they want to delete all the books existing in the inventory.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.action_delete_all_entries);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete all books.
                deleteAllBooks();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and don't change anything.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                bookEntry._ID,
                bookEntry.COLUMN_BOOK_TITLE,
                bookEntry.COLUMN_BOOK_AUTHOR,
                bookEntry.COLUMN_BOOK_PRICE,
                bookEntry.COLUMN_BOOK_QUANTITY};
        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                bookEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link BooksCursorAdapter} with this new cursor containing updated book data
        mCursorAdapter.swapCursor(data);
        // If the book inventory is empty, animate the add-book button so it is very clear what the user should do in order to add new books
        if (mCursorAdapter.getCount() == 0)
            flashingFab();
            // If the book inventory is not empty, the add-book animation is not needed, since the user must know what to do in order to add new books..
        else
            addBookButton.clearAnimation();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }

    /**
     * Animation method that makes the add book button flash
     */
    public void flashingFab() {
        // Change alpha from fully visible to partially transparent
        final Animation animation = new AlphaAnimation(1, Float.parseFloat("0.3"));
        // Duration- one second
        animation.setDuration(1500);
        // Do not alter animation rate
        animation.setInterpolator(new LinearInterpolator());
        // Repeat animation infinitely
        animation.setRepeatCount(Animation.INFINITE);
        // Reverse animation at the end so the button will fade back in
        animation.setRepeatMode(Animation.REVERSE);
        // Start animation on that button
        addBookButton.startAnimation(animation);
    }
}








