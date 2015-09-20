package org.vanbest.subwayedit;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.chainfire.libsuperuser.Shell;

public class EditActivity extends AppCompatActivity {
    private static final String filename = "/data/data/com.kiloo.subwaysurf/files/playerdata.0";
    private static final String onlineSettingsFilename = "/data/data/com.kiloo.subwaysurf/files/onlinesettings";
    private static final String toprunManagerFilename = "/data/data/com.kiloo.subwaysurf/files/toprun_manager";
    private static final String toprunPlayerIdsFilename = "/data/data/com.kiloo.subwaysurf/files/toprun_playerids";
    private static final String toprunRanksFilename = "/data/data/com.kiloo.subwaysurf/files/toprun_ranks";

    private String localFilename;
    private SubwayFile file;
    private String fileOwnerInfo = null;
    private Map<Integer,EditText> views;
    private Map<Integer,EditText> numbers1_views;
    private Map<Integer,EditText> numbers2_views;

    private class StartUp extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            Button saveButton = (Button) findViewById(R.id.edit_save_button);
            saveButton.setEnabled(false);
        }
        @Override
        protected String doInBackground(String... params) {
            file = null;
            // publishProgress("Please wait...");
            Log.d("SubwayFile", "Original data file:" + filename);
            Log.d("SubwayFile", "Local data file:" + localFilename);

            publishProgress("Getting root access...");
            try {
                Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "ls " + filename });
                process.waitFor();
            } catch (IOException e3) {
                Log.d("SubwayFile", "IOException running su manually", e3);
            } catch (InterruptedException e4) {
                Log.d("SubwayFile", e4.toString());
            }
            Log.d("SubwayFile", "B");

            boolean suAvailable = Shell.SU.available();
            if (suAvailable) {
                // suVersion = Shell.SU.version(false);
                // suVersionInternal = Shell.SU.version(true);
                // publishProgress("Copying file...");
                Log.d("SubwayEdit", "Running su to copy player data file");
                Log.d("SubwayEdit", "dataDir: " + getApplicationInfo().dataDir);
                List<String> suResult = Shell.SU.run(new String[] {
                        "ls -ln " + filename,
                        "echo 1:",
                        "cat \"" + filename + "\" >\"" + localFilename + "\"",
                        "echo 2:",
                        "chmod 0666 " + localFilename,
                        "echo 3:",
                        "ls -ln " + filename,
                        "echo 4:",
                        "ls -al " + localFilename,
                        "echo 5:",
                        "ls -al " + getApplicationInfo().dataDir,
                        "echo 6"
                });
                Log.d("SubwayFile", "Su result: ");
                for(String s: suResult) {
                    Log.d("SubwayFile", s);
                }
                if (suResult.size()>0) {
                    Log.d("SubwayFile", "String to match: [" + suResult.get(0) + "]");
                    Pattern p = Pattern.compile("^\\S*\\s*(\\d+)\\s+(\\d+)\\s+.*");
                    Matcher m = p.matcher(suResult.get(0));
                    if (m.matches()) {
                        Log.d("SubwayFile", "Matched owner:group is " + m.group(1) + ":" + m.group(2));
                        fileOwnerInfo = m.group(1) + ":" + m.group(2);
                    } else {
                        Log.d("SubwayFile", "No match");
                    }
                }
                publishProgress("Parsing settings file...");
                try {
                    Log.d("SubwayEdit", "CreatingSubwayFile object");
                    file = new SubwayFile(localFilename);
                } catch (IOException e2) {
                    Log.d("SubwayFile", e2.toString());
                }
            } else {
                Log.d("SubwayFile", "No root available");
            }

            publishProgress("Preparing display...");
            return null;
        }

        protected void onProgressUpdate(String... result) {
            TableLayout table = (TableLayout) findViewById(R.id.tableLayout);
            Context context = table.getContext();
            Toast.makeText(context, result[0], Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            TableLayout table = (TableLayout) findViewById(R.id.tableLayout);
            Context context = table.getContext();

            if ( file != null ) {
                Log.d("SubwayEdit", "Creating edit fields...");

                views = new HashMap<>();
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

                    views.put(i, edit);

                    table.addView(row);
                }

                TableRow heading = new TableRow(context);
                TextView heading_label= new TextView(context);
                heading_label.setText("Numerical settings");
                heading_label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                heading.addView(heading_label);
                table.addView(heading);

                numbers1_views = new HashMap<>();
                for (int i=0; i<file.getNumbers1Size(); i++) {
                    TableRow row = new TableRow(context);

                    TextView label  = new TextView(context);
                    label.setText(file.getNumbers1Key(i));
                    label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                    row.addView(label);

                    EditText edit = new EditText(context);
                    edit.setText(Integer.toString(file.getNumbers1Value(i)));
                    edit.setLines(1);
                    edit.setSingleLine();
                    row.addView(edit);

                    numbers1_views.put(i, edit);

                    table.addView(row);
                }

                numbers2_views = new HashMap<>();
                for (int i=0; i<file.getNumbers1Size(); i++) {
                    TableRow row = new TableRow(context);

                    TextView label  = new TextView(context);
                    label.setText(file.getNumbers2Key(i));
                    label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                    row.addView(label);

                    EditText edit = new EditText(context);
                    edit.setText(Integer.toString(file.getNumbers2Value(i)));
                    edit.setLines(1);
                    edit.setSingleLine();
                    row.addView(edit);

                    numbers2_views.put(i, edit);

                    table.addView(row);
                }
            }
            Log.d("SubwayEdit", "Done creating edit fields");

            Button saveButton = (Button) findViewById(R.id.edit_save_button);
            saveButton.setEnabled(true);
        }
    }

    private class SaveSettings extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            //file.setValue(0, "123456");
            if (file == null) return null;

            try {
                file.writeFile(localFilename);
            } catch (IOException ex) {
                Log.d("SubwayMainAct", "Error writing to local file", ex);
                return null;
            }

            boolean suAvailable = Shell.SU.available();
            List<String> suResult2 = Shell.SU.run(new String[]{
                    "cat \"" + localFilename + "\" > \"" + filename + "\"",
                    "ls -l " + filename,
                    "chmod 0600 " + filename,
                    fileOwnerInfo == null ? "echo no" : "chown " + fileOwnerInfo + " " + filename,
                    "ls -l " + filename
            });
            Log.d("SubwayFile", "Su result copying back: ");
            for (String s : suResult2) {
                Log.d("SubwayFile", s);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            TableLayout table = (TableLayout) findViewById(R.id.tableLayout);
            Context context = table.getContext();
            Toast.makeText(context, "Settings saved", Toast.LENGTH_SHORT).show();
        }
    }

    public void saveSettings(View view) {

        if (views != null) {
            for (int i: views.keySet()) {
                file.setValue(i, views.get(i).getText().toString());
            }
        }
        if (numbers1_views != null) {
            for (int i: numbers1_views.keySet()) {
                file.setNumbers1Value(i, Integer.parseInt(numbers1_views.get(i).getText().toString()));
            }
        }
        if (numbers2_views != null) {
            for (int i: numbers2_views.keySet()) {
                file.setNumbers2Value(i, Integer.parseInt(numbers2_views.get(i).getText().toString()));
            }
        }
        new SaveSettings().execute();

        this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        localFilename = getApplicationInfo().dataDir+"/playerdata";
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
