package org.vanbest.subwayedit;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.chainfire.libsuperuser.Shell;

public class MainActivity extends AppCompatActivity {
    private static final String filename = "/data/data/com.kiloo.subwaysurf/files/playerdata.0";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GridView gridview = (GridView) findViewById(R.id.gridview);

        SubwayFile f = null;
        f = new SubwayFile();
        try {
            f = new SubwayFile(filename);
        } catch (IOException e) {
            Log.d("SubwayFile", e.toString());

            String localfilename = getApplicationInfo().dataDir+"/playerdata";
            Log.d("SubwayFile", "Data file:" + localfilename);

            try {
                Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "ls " + filename });
                process.waitFor();
            } catch (IOException e3) {
                Log.d("SubwayFile", e3.toString());
            } catch (InterruptedException e4) {
                Log.d("SubwayFile", e4.toString());
            }
            Log.d("SubwayFile", "B");

            boolean suAvailable = Shell.SU.available();
            if (suAvailable) {
                // suVersion = Shell.SU.version(false);
                // suVersionInternal = Shell.SU.version(true);
                List<String> suResult = Shell.SU.run(new String[] {
                        "cp " + filename + " " + localfilename,
                        "chmod 0644 " + localfilename
                });
                Log.d("SubwayFile", "Su result: ");
                for(String s: suResult) {
                    Log.d("SubwayFile", s);
                }
                try {
                    f = new SubwayFile(localfilename);
                } catch (IOException e2) {
                    Log.d("SubwayFile", e2.toString());
                }
            } else {
                Log.d("SubwayFile", "No root available");
            }

        }

        gridview.setAdapter(new SubwayAdapter(this, f));

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(getApplicationContext(), "" + position,
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
