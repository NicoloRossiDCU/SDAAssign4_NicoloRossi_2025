package com.example.sdaassign4_2022;

        /*
         * Copyright (C) 2016 The Android Open Source Project
         *
         * Licensed under the Apache License, Version 2.0 (the "License");
         * you may not use this file except in compliance with the License.
         * You may obtain a copy of the License at
         *
         *      http://www.apache.org/licenses/LICENSE-2.0
         *
         * Unless required by applicable law or agreed to in writing, software
         * distributed under the License is distributed on an "AS IS" BASIS,
         * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
         * See the License for the specific language governing permissions and
         * limitations under the License.
         */

        import static android.content.Context.MODE_PRIVATE;
        import static android.provider.Settings.System.getString;


        import android.content.Context;
        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.Button;
        import android.widget.ImageView;
        import android.widget.RelativeLayout;
        import android.widget.TextView;
        import android.widget.Toast;
        import androidx.fragment.app.Fragment;

        import androidx.annotation.NonNull;
        import androidx.recyclerview.widget.RecyclerView;

        import com.bumptech.glide.Glide;

        import java.util.ArrayList;


/*
 * @author Chris Coughlan 2019
 */
public class LibraryViewAdapter extends RecyclerView.Adapter<LibraryViewAdapter.ViewHolder> {
    private static final String TAG = "RecyclerViewAdapter";
    private Context mNewContext;
    private SharedPreferences prefs;
    private ArrayList<Book> mBooks;

    LibraryViewAdapter(Context mNewContext,ArrayList<Book> books) {
        this.mNewContext = mNewContext;
        this.mBooks = books;
    }

    //declare methods
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_list_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        // get the book selected with all the properties
        Book selectedBook = mBooks.get(viewHolder.getAdapterPosition());

        boolean available = selectedBook.Available;

        // display book
        viewHolder.authorText.setText(selectedBook.Author);
        viewHolder.titleText.setText( selectedBook.Title);

        // glide use the url to get the image from the db
        Glide.with(mNewContext)
                .load(selectedBook.Url)
                .into(viewHolder.imageItem).waitForLayout();

        viewHolder.checkOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                // we need to get the user settings to check them and to use them to borrow the book
                prefs = mNewContext.getSharedPreferences("userdetails", MODE_PRIVATE);
                String settingUsername = prefs.getString(SharedPreferencesSettings.USER_NAME.name(), "");
                String settingEmail = prefs.getString(SharedPreferencesSettings.EMAIL.name(), "");
                String settingUserId = prefs.getString(SharedPreferencesSettings.USER_ID.name(), "");

                boolean isSettings =
                        !(settingUsername.isEmpty() || settingEmail.isEmpty() || settingUserId.isEmpty());

                // if the settings are all saved
                if(isSettings) {
                    // start intent with all data that will be needed
                    Intent myOrder = new Intent(mNewContext, CheckOut.class);
                    myOrder.putExtra("title", selectedBook.Title);
                    myOrder.putExtra("id", selectedBook.ID);

                    myOrder.putExtra("Available", available);
                    myOrder.putExtra("lastBorrow", selectedBook.DateLastBorrow.toString());

                    mNewContext.startActivity(myOrder);
                } else {
                    // user needs to insert his data to borrow book
                    Toast.makeText(mNewContext, mNewContext.getResources().getString(R.string.setup_account_message), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mBooks.size();
    }

    //view holder class for recycler_list_item.xml
    class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imageItem;
        TextView authorText;
        TextView titleText;
        Button checkOut;
        RelativeLayout itemParentLayout;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            //grab the image, the text and the layout id's
            imageItem = itemView.findViewById(R.id.bookImage);
            authorText = itemView.findViewById(R.id.authorText);
            titleText = itemView.findViewById(R.id.bookTitle);
            checkOut = itemView.findViewById(R.id.out_button);
            itemParentLayout = itemView.findViewById(R.id.listItemLayout);

        }
    }
}
