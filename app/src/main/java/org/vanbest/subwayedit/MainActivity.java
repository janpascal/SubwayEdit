package org.vanbest.subwayedit;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
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

    private class StartUp extends AsyncTask<String, String, String> {

        private SubwayFile file;

        @Override
        protected String doInBackground(String... params) {
            file = null;
            try {
                file = new SubwayFile(filename);
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
                            "chmod 0666 " + localfilename,
                            "ls -l " + localfilename
                    });
                    Log.d("SubwayFile", "Su result: ");
                    for(String s: suResult) {
                        Log.d("SubwayFile", s);
                    }
                    try {
                        file = new SubwayFile(localfilename);
                    } catch (IOException e2) {
                        Log.d("SubwayFile", e2.toString());
                    }

                    file.setValue(0, "123456");
                    try {
                        file.writeFile(localfilename);
                    } catch (IOException ex) {
                        Log.d("SubwayMainAct", "Error writing to local file", ex);
                        return null;
                    }

                    suAvailable = Shell.SU.available();
                    List<String> suResult2 = Shell.SU.run(new String[] {
                            "cp " + localfilename + " " + filename,
                            "chmod 0644 " + localfilename,
                            "chown u0_a67:u0_a67 " + filename,
                            "ls -l " + filename
                    });
                    Log.d("SubwayFile", "Su result copying back: ");
                    for(String s: suResult2) {
                        Log.d("SubwayFile", s);
                    }


                } else {
                    Log.d("SubwayFile", "No root available");
                }


            }

            return null;
        }

        protected void onProgressUpdate(String... result) {
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            TableLayout table = (TableLayout) findViewById(R.id.tableLayout);

            Context context = table.getContext();
            if ( file != null ) {
                for (int i=0; i<file.getSize(); i++) {
                    TableRow row = new TableRow(context);

                    TextView label  = new TextView(context);
                    label.setText(file.getKey(i));
                    label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                    row.addView(label);

                    EditText edit = new EditText(context);
                    edit.setText(file.getValue(i));
                    edit.setLines(1);
                    edit.setSingleLine();
                    row.addView(edit);

                    table.addView(row);
                }
            }

        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new StartUp().execute();
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
