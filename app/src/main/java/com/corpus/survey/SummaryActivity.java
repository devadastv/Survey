package com.corpus.survey;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.corpus.survey.db.SurveySQLiteHelper;
import com.corpus.survey.usermanagement.UserProfileManager;

public class SummaryActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SurveySQLiteHelper dbHelper = new SurveySQLiteHelper(this);
    private boolean doubleBackToExitPressedOnce = false;
    NavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSurvey();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        updateSummaryMessage();
        updateUserInfoInNavigationView();
    }

    private void updateSummaryMessage() {
        String welcomeText = "" + UserProfileManager.getInstance().getUserName(this) + "\n\nTotal number of surveys : " + dbHelper.getNumberOfSurveyEntries();
        TextView mSummaryText = (TextView) findViewById(R.id.summary_text);
        mSummaryText.setText(welcomeText);
    }

    private void updateUserInfoInNavigationView() {
        ImageView mUserImage = (ImageView) findViewById(R.id.profile_user_image);
        mUserImage.setImageResource(UserProfileManager.getInstance().getUserImageId());

        TextView mUserName = (TextView) findViewById(R.id.profile_user_name);
        mUserName.setText(UserProfileManager.getInstance().getUserName(this));

        TextView mUserEmail = (TextView) findViewById(R.id.profile_user_email);
        mUserEmail.setText(UserProfileManager.getInstance().getUserEmail());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.new_survey) {
            startSurvey();
        } else if (id == R.id.admin_login) {
            displayAdminPasswordEntry();
        } else if (id == R.id.admin_logout) {
            hideAdminMenuItems();
        } else if (id == R.id.survey_list) {
            launchSurveyList();
        } else if (id == R.id.clear_surveys) {
            displayClearSurveyConfirmationDialog();
        } else if (id == R.id.action_settings) {
            Intent contentSummaryIntent = new Intent(this, SettingsActivity.class);
            startActivity(contentSummaryIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void hideAdminMenuItems() {
        navigationView.getMenu().findItem(R.id.admin_login).setVisible(true);
        navigationView.getMenu().findItem(R.id.admin_logout).setVisible(false);
        navigationView.getMenu().setGroupVisible(R.id.admin_user_menu_group, false);
        Toast.makeText(SummaryActivity.this, "Admin successfully logged out", Toast.LENGTH_SHORT).show();
    }

    private void displayAdminPasswordEntry() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Admin Password");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setPadding(50, 20, 50, 20);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String entry = input.getText().toString();
                if (entry.equals(LoginActivity.ADMIN_PASSWORD)) {
                    Toast.makeText(SummaryActivity.this, "Granted Admin Access!", Toast.LENGTH_SHORT).show();
                    navigationView.getMenu().findItem(R.id.admin_login).setVisible(false);
                    navigationView.getMenu().findItem(R.id.admin_logout).setVisible(true);
                    navigationView.getMenu().setGroupVisible(R.id.admin_user_menu_group, true);
                } else {
                    Toast.makeText(SummaryActivity.this, "Incorrect password. Try again", Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void launchSurveyList() {
        Intent intent = new Intent(SummaryActivity.this, SurveyListActivity.class);
        startActivity(intent);
    }

    private void startSurvey() {
        Intent contentSummaryIntent = new Intent(this, SurveyActivity.class);
        startActivity(contentSummaryIntent);
    }

    private void displayClearSurveyConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Warning: Clearing all Surveys")
                .setMessage("Are you sure you want to delete all survey entries created so far? This action can not be undone!")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.deleteAllSurveyEntries();
                        updateSummaryMessage();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onResume() {
        Log.d("SummaryActivity", "Inside onResume of SummaryActivity");
        updateSummaryMessage();
        updateUserInfoInNavigationView();
        super.onResume();
    }
}
