package com.nano.popularmovies;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {


    Bitmap[] imgs = new Bitmap[10];
    GridView gridview;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        new loadImgs().execute();
        gridview = (GridView) findViewById(R.id.gridview);


        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(MainActivity.this, "" + position,
                        Toast.LENGTH_SHORT).show();
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.


        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {

            case R.id.refresh:
                new loadImgs().execute();

                break;

            case R.id.menuSortNewest:

                break;

            case R.id.menuSortRating:

                break;
        }


        return super.onOptionsItemSelected(item);
    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        // references to our images


        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return imgs.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            /*ImageView imageView;
            View view = null;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes

                 view = getLayoutInflater().inflate(R.layout.grid_item, null);
                TextView text = (TextView) view.findViewById(R.id.tvGrid);
                text.setText("Test");

                 imageView = (ImageView) view.findViewById(R.id.ivGrid);
                imageView.setLayoutParams(new GridView.LayoutParams(300, 400));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }

           // imageView.setImageResource(mThumbIds[position]);
            imageView.setImageBitmap(imgs[position]);
            return imageView;*/

            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View gridView;

            if (convertView == null) {

                gridView = new View(mContext);

                // get layout from mobile.xml
                gridView = inflater.inflate(R.layout.grid_item, null);

                // set image based on selected text
                ImageView imageView = (ImageView) gridView
                        .findViewById(R.id.ivGrid);


                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

                imageView.setPadding(8, 8, 8, 8);

                TextView tvGrid = (TextView) gridView.findViewById(R.id.tvGrid);
                tvGrid.setText("Movie #" + (position + 1));


                imageView.setImageBitmap(imgs[position]);


            } else {
                gridView = convertView;
            }

            return gridView;
        }


    }

    class loadImgs extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub

            super.onPreExecute();

            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Loading Movies Info..");
            pd.setCanceledOnTouchOutside(false);
            pd.setIndeterminate(false);
            pd.show();



        }

        @Override
        protected String doInBackground(String... arg0) {
            // TODO Auto-generated method stub
            for (int i = 0; i < 10; i++) {

                try {
                    imgs[i] = Picasso.with(getApplicationContext()).load("http://image.tmdb.org/t/p/w185//nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg").get();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub

            gridview.setAdapter(new ImageAdapter(getApplicationContext()));
            pd.dismiss();

        }
    }
}
