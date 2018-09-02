package com.example.android.p9inventoryappstage2;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import com.example.android.p9inventoryappstage2.data.bookContract.bookEntry;

import java.util.Currency;
import java.util.Locale;

/**
 * Allows user to create a new book or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * Identifier for the book data loader
     */
    private static final int EXISTING_BOOK_LOADER = 0;
    /**
     * Content URI for the existing book (null if it's a new book)
     */
    private Uri mCurrentBookUri;
    /**
     * EditText field to enter the book's title
     */
    private EditText mTitleEditText;
    /**
     * EditText field to enter the book's author
     */
    private EditText mAuthorEditText;
    /**
     * EditText field to enter the book's price
     */
    private EditText mPriceEditText;
    /**
     * EditText field to enter the book's quantity in stock
     */
    private EditText mQuntityEditText;
    /**
     * EditText field to enter the book's supplier name
     */
    private EditText mSupplierNameEditText;
    /**
     * EditText field to enter the book's supplier phone number
     */
    private EditText mSupplierPhoneEditText;
    /**
     * TextView to diplay the relevant currency code in the enter price field
     */
    private TextView mCurrencyCodeTextView;
    /**
     * EditText field to enter the increment/decrement amount the user would like apply when chooses to use the plus/minus buttons to update the book's quantity in sock
     */
    private EditText mQuantityIncDecAmount;
    /**
     * ImageButton to increase the book's quantity by mQuantityIncDecAmount
     */
    private ImageButton mQuantityIncrease;
    /**
     * ImageButton to decrease the book's quantity by mQuantityIncDecAmount
     */
    private ImageButton mQuantityDecrease;
    /**
     * FloatingActionButton to open a phone app with the supplier's phone number
     */
    private FloatingActionButton mSupplierPhoneCallFAB;
    /**
     * String variable that contains the supplier's phone number and will be used in the phone app
     */
    private String mSupplierPhone;
    /**
     * Boolean flag that keeps track of whether the book has been edited (true) or not (false)
     */
    private boolean mBookHasChanged = false;
    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mBookHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        // Examine the intent that was used to launch this activity, in order to figure out if we're creating a new book
        // or editing an existing one.
        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();
        // Find all relevant views that we will need to display if it's "edit book" mode, and hide if it's "add book" mode
        mQuantityIncDecAmount = (EditText) findViewById(R.id.amount_increase_or_decrease);
        mQuantityIncrease = (ImageButton) findViewById(R.id.quantity_increase);
        mQuantityDecrease = (ImageButton) findViewById(R.id.quantity_decrease);
        mSupplierPhoneCallFAB = (FloatingActionButton) findViewById(R.id.fab_order_from_supplier);
        mCurrencyCodeTextView = (TextView) findViewById(R.id.price_symbol);
        TextInputLayout amountIncDecContainer= (TextInputLayout) findViewById(R.id.amount_increase_or_decrease_container);
        // Set the currency code to the relevant value
        mCurrencyCodeTextView.setText(Currency.getInstance(Locale.getDefault()).getCurrencyCode());
        // Set default value for decrement/increment amount to 1
        mQuantityIncDecAmount.setText(Integer.toString(1));
        // If the intent DOES NOT contain a book content URI, then we know that we are creating a new book.
        if (mCurrentBookUri == null) {
            // This is a new book, so change the app bar to say "Add a Book"
            setTitle(getString(R.string.editor_activity_title_new_book));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a book that hasn't been created yet.)
            invalidateOptionsMenu();
            // Hide the views that are irrelevant when adding a new book (inc/dec amount, inc and dec buttons, call supplier button)
            mQuantityDecrease.setVisibility(View.GONE);
            mQuantityIncrease.setVisibility(View.GONE);
            mSupplierPhoneCallFAB.setVisibility(View.GONE);
            amountIncDecContainer.setVisibility(View.GONE);
        } else {
            // Otherwise this is an existing book, so change app bar to say "Edit Book"
            setTitle(getString(R.string.editor_activity_title_edit_book));
            // Display the views that are relevant when watching the details or editing an existing book (inc/dec amount, inc and dec buttons, call supplier button)
            mQuantityDecrease.setVisibility(View.VISIBLE);
            mQuantityIncrease.setVisibility(View.VISIBLE);
            mSupplierPhoneCallFAB.setVisibility(View.VISIBLE);
            amountIncDecContainer.setVisibility(View.VISIBLE);
            // Initialize a loader to read the book data from the database and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        }
        // Find all relevant views that we will need to read user input from
        mTitleEditText = (EditText) findViewById(R.id.edit_book_title);
        mAuthorEditText = (EditText) findViewById(R.id.edit_book_author);
        mPriceEditText = (EditText) findViewById(R.id.edit_book_price);
        mQuntityEditText = (EditText) findViewById(R.id.edit_book_quantity);
        mSupplierNameEditText = (EditText) findViewById(R.id.edit_book_supplier_name);
        mSupplierPhoneEditText = (EditText) findViewById(R.id.edit_book_supplier_phone);
        // Setup OnTouchListeners on all the input fields, so we can determine if the user has touched or modified them.
        // This will let us know if there are unsaved changes or not, if the user tries to leave the editor without saving.
        mTitleEditText.setOnTouchListener(mTouchListener);
        mAuthorEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuntityEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneEditText.setOnTouchListener(mTouchListener);
        // Executed when the "-" button is clicked
        mQuantityDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.parseInt(mQuntityEditText.getText().toString());
                // In case the user deleted the default value and didn't type a new value, set it again
                if (TextUtils.isEmpty(mQuantityIncDecAmount.getText()))
                    mQuantityIncDecAmount.setText(Integer.toString(1));
                int amount = Integer.parseInt(mQuantityIncDecAmount.getText().toString());
                // If quantity has at least the same value as decrement amount, subtract that amount from the quantity
                if (quantity >= amount)
                    mQuntityEditText.setText(Integer.toString(quantity - amount));
                    // If quantity's value is smaller than the decrement amount, it is impossible to decrease the entire amount, so we will let the user know that he cannot sell that amount
                else if (quantity > 0 && quantity < amount) {
                    Toast.makeText(EditorActivity.this, getText(R.string.decrement_error_dec_amount_bigger_than_quantity).toString() + quantity + "!", Toast.LENGTH_LONG).show();
                    // if quantity's value is 0, this book is out of stock, so there's nothing to decrease any amount from
                } else if (quantity == 0) {
                    Toast.makeText(EditorActivity.this, getText(R.string.decrement_error_quantity_is_zero), Toast.LENGTH_LONG).show();
                    // Make the call supplier button flash so it is clear what the user should do if he wants to order the book from its supplier
                    flashingFab();
                }
            }
        });
        // Executed when the "+" button is clicked
        mQuantityIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.parseInt(mQuntityEditText.getText().toString());
                // In case the user deleted the default value and didn't type a new value, set it again
                if (TextUtils.isEmpty(mQuantityIncDecAmount.getText()))
                    mQuantityIncDecAmount.setText(Integer.toString(1));
                int amount = Integer.parseInt(mQuantityIncDecAmount.getText().toString());
                // Add the amount to the quantity
                mQuntityEditText.setText(Integer.toString(quantity + amount));
            }
        });
        // Executed when the "call supplier" button is clicked
        mSupplierPhoneCallFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // In case the user deleted the phone number without typing a new one, set it again
                if (TextUtils.isEmpty(mSupplierPhoneEditText.getText()))
                    mSupplierPhoneEditText.setText(mSupplierPhone);
                String supplierPhoneNumber = mSupplierPhoneEditText.getText().toString();
                // Send an intent to open a phone app with the supplier's phone number dialed in it
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + supplierPhoneNumber));
                startActivity(intent);
            }
        });
    }

    /**
     * Get user input from editor and save book into database.
     */
    private boolean saveBook() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String titleString = mTitleEditText.getText().toString().trim();
        String authorString = mAuthorEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuntityEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierPhoneString = mSupplierPhoneEditText.getText().toString().trim();
        // Check if this is supposed to be a new book, and check if all the fields in the editor are blank
        if (mCurrentBookUri == null &&
                TextUtils.isEmpty(titleString) && TextUtils.isEmpty(authorString) &&
                TextUtils.isEmpty(priceString) && TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(supplierNameString) && TextUtils.isEmpty(supplierPhoneString)) {
            // Since no fields were modified, we can return early without creating a new book.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            Toast.makeText(this, getText(R.string.save_error_all_fields_blank), Toast.LENGTH_LONG).show();
            return false;
        }
        // Check every particular field. if the field is empty or contains an invalid value, focus on it,
        // and display an error message that tells the user how to do it correctly
        if (TextUtils.isEmpty(titleString)) {
            mTitleEditText.requestFocus();
            mTitleEditText.setError(getText(R.string.save_error_title_field_blank));
            return false;
        }
        if (TextUtils.isEmpty(authorString)) {
            mAuthorEditText.requestFocus();
            mAuthorEditText.setError(getText(R.string.save_error_author_field_blank));
            return false;
        }
        if (TextUtils.isEmpty(priceString)) {
            mPriceEditText.requestFocus();
            mPriceEditText.setError(getText(R.string.save_error_price_field_blank));
            return false;
        }
        if (Double.parseDouble(priceString) <= Double.parseDouble("0")) {
            mPriceEditText.requestFocus();
            mPriceEditText.setError(getText(R.string.save_error_price_field_invalid));
            return false;
        }
        if (TextUtils.isEmpty(quantityString)) {
            mQuntityEditText.requestFocus();
            mQuntityEditText.setError(getText(R.string.save_error_quantity_field_blank));
            return false;
        } else if ((Integer.parseInt(quantityString) < 0)) {
            mQuntityEditText.requestFocus();
            mQuntityEditText.setError(getText(R.string.save_error_quantity_field_invalid));
            return false;
        }
        if (TextUtils.isEmpty(supplierNameString)) {
            mSupplierNameEditText.requestFocus();
            mSupplierNameEditText.setError(getText(R.string.save_error_supplier_name_field_blank));
            return false;
        }
        if (TextUtils.isEmpty(supplierPhoneString)) {
            mSupplierPhoneEditText.requestFocus();
            mSupplierPhoneEditText.setError(getText(R.string.save_error_supplier_phone_field_blank));
            return false;
        }
        // Create a ContentValues object where column names are the keys, and book attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(bookEntry.COLUMN_BOOK_TITLE, titleString);
        values.put(bookEntry.COLUMN_BOOK_AUTHOR, authorString);
        values.put(bookEntry.COLUMN_BOOK_PRICE, priceString);
        values.put(bookEntry.COLUMN_BOOK_QUANTITY, quantityString);
        values.put(bookEntry.COLUMN_BOOK_SUPPLIER_NAME, supplierNameString);
        values.put(bookEntry.COLUMN_BOOK_SUPPLIER_PHONE, supplierPhoneString);
        // Determine if this is a new or existing book by checking if mCurrentPetUri is null or not
        if (mCurrentBookUri == null) {
            // This is a NEW book, so insert a new book into the provider, returning the content URI for the new book.
            Uri newUri = getContentResolver().insert(bookEntry.CONTENT_URI, values);
            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING book, so update the book with content URI: mCurrentBookUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentBookUri will already identify the correct row in the database that we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        MenuItem menuItem = menu.findItem(R.id.action_save);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new book, hide the "Delete" menu item.
        if (mCurrentBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // If all inserted/edited fields are filled and valid, save book to database
                if (saveBook()) {
                    startActivity(new Intent(EditorActivity.this, CatalogActivity.class));
                    // Exit activity
                    finish();
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the book hasn't changed, continue with navigating up to parent activity which is the {@link CatalogActivity}.
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the book hasn't changed, continue with handling back button press
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all book attributes, define a projection that contains all columns from the book table
        String[] projection = {
                bookEntry._ID,
                bookEntry.COLUMN_BOOK_TITLE,
                bookEntry.COLUMN_BOOK_AUTHOR,
                bookEntry.COLUMN_BOOK_PRICE,
                bookEntry.COLUMN_BOOK_QUANTITY,
                bookEntry.COLUMN_BOOK_SUPPLIER_NAME,
                bookEntry.COLUMN_BOOK_SUPPLIER_PHONE};
        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentBookUri,         // Query the content URI for the current book
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of book attributes that we're interested in
            int titleColumnIndex = cursor.getColumnIndex(bookEntry.COLUMN_BOOK_TITLE);
            int authorColumnIndex = cursor.getColumnIndex(bookEntry.COLUMN_BOOK_AUTHOR);
            int priceColumnIndex = cursor.getColumnIndex(bookEntry.COLUMN_BOOK_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(bookEntry.COLUMN_BOOK_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(bookEntry.COLUMN_BOOK_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(bookEntry.COLUMN_BOOK_SUPPLIER_PHONE);
            // Extract out the value from the Cursor for the given column index
            String title = cursor.getString(titleColumnIndex);
            String author = cursor.getString(authorColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            mSupplierPhone = cursor.getString(supplierPhoneColumnIndex);
            // Update the views on the screen with the values from the database
            mTitleEditText.setText(title);
            mAuthorEditText.setText(author);
            mPriceEditText.setText(String.format(Locale.getDefault(), "%.2f", price));
            mQuntityEditText.setText(Integer.toString(quantity));
            mSupplierNameEditText.setText(supplierName);
            mSupplierPhoneEditText.setText(mSupplierPhone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mTitleEditText.setText("");
        mAuthorEditText.setText("");
        mPriceEditText.setText("");
        mQuntityEditText.setText("");
        mSupplierNameEditText.setText("");
        mSupplierPhoneEditText.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners for the postivie and negative buttons
        // on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this book.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the book.
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the book in the database.
     */
    private void deleteBook() {
        // Only perform the delete if this is an existing book.
        if (mCurrentBookUri != null) {
            // Call the ContentResolver to delete the book at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentBookUri
            // content URI already identifies the book that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }

    /**
     * Animation method that makes the call supplier phone flash
     */
    public void flashingFab() {
        // Change alpha from fully visible to partially transparent
        final Animation animation = new AlphaAnimation(1, Float.parseFloat("0.3"));
        // Duration- one second and a half
        animation.setDuration(1500);
        // Do not alter animation rate
        animation.setInterpolator(new LinearInterpolator());
        // Repeat animation 3 times
        animation.setRepeatCount(4);
        // Reverse animation at the end so the button will fade back in
        animation.setRepeatMode(Animation.REVERSE);
        // Start animation on that button
        mSupplierPhoneCallFAB.startAnimation(animation);
    }
}