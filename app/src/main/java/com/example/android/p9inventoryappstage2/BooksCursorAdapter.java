package com.example.android.p9inventoryappstage2;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.p9inventoryappstage2.data.bookContract.bookEntry;

import android.widget.CursorAdapter;

import java.text.NumberFormat;

/**
 * {@link BooksCursorAdapter} is an adapter for a list or grid view that uses a {@link Cursor} of book data as its data source.
 * This adapter knows how to create list items for each row of book data in the {@link Cursor}.
 */
public class BooksCursorAdapter extends CursorAdapter {
    /**
     * Constructs a new {@link BooksCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public BooksCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the book data (in the current row pointed to by cursor) to the given list item layout.
     * For example, the title for the current book can be set on the title TextView in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView titleTextView = (TextView) view.findViewById(R.id.title);
        TextView authorTextView = (TextView) view.findViewById(R.id.author);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView stockTextView = (TextView) view.findViewById(R.id.stock);
        FloatingActionButton sellBookButton = (FloatingActionButton) view.findViewById(R.id.fab_sold_book);
        final int rowColumnIndex = cursor.getInt(cursor.getColumnIndex(bookEntry._ID));
        // Find the columns of book attributes that we're interested in
        int titleColumnIndex = cursor.getColumnIndex(bookEntry.COLUMN_BOOK_TITLE);
        int authorColumnIndex = cursor.getColumnIndex(bookEntry.COLUMN_BOOK_AUTHOR);
        int priceColumnIndex = cursor.getColumnIndex(bookEntry.COLUMN_BOOK_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(bookEntry.COLUMN_BOOK_QUANTITY);
        // Read the book attributes from the Cursor for the current book
        String bookTitle = cursor.getString(titleColumnIndex);
        String bookAuthor = cursor.getString(authorColumnIndex);
        String bookPrice = cursor.getString(priceColumnIndex);
        final String bookQuantity = cursor.getString(quantityColumnIndex);
        // Update the TextViews with the attributes for the current book
        titleTextView.setText(bookTitle);
        authorTextView.setText(bookAuthor);
        // The price is displayed with the currency symbol
        priceTextView.setText(NumberFormat.getCurrencyInstance().format(Double.parseDouble(bookPrice)));
        // If the quantity is bigger than 0, display "quantity available"
        if (Integer.parseInt(bookQuantity) > 0) {
            stockTextView.setText(bookQuantity + " available");
            stockTextView.setTextColor(ContextCompat.getColor(context,R.color.listItemTextColor));
        }
        // If the quantity is 0, display "Out of stock" in red color
        else if (Integer.parseInt(bookQuantity) == 0) {
            stockTextView.setText("Out of stock");
            stockTextView.setTextColor(Color.RED);
        }
        // Execute when the "sell" button is clicked
        sellBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Getting the URI with the append of the ID for the row
                Uri uri = ContentUris.withAppendedId(bookEntry.CONTENT_URI, rowColumnIndex);
                // If the quantity is bigger than 0, this book can be sold
                if (Integer.parseInt(bookQuantity) > 0) {
                    // Get the current value of quantity and update it with the new value after one book was sold: quantity-1
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(bookEntry.COLUMN_BOOK_QUANTITY, (Integer.parseInt(bookQuantity) - 1));
                    context.getContentResolver().update(uri, contentValues, null, null);
                }
                // If the quantity is 0, this book can't be sold, so nothing changes and the user gets the following message
                else if (Integer.parseInt(bookQuantity) == 0) {
                    Toast.makeText(context, "This book is OUT OF STOCK! you cannot sell it at the moment", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}