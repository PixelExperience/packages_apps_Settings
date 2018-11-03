/*
 * Copyright (C) 2018 PixelExperience
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses>.
 */

package com.android.settings.custom.ambient.history;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.internal.util.ambient.play.AmbientPlayHistoryEntry;
import com.android.internal.util.ambient.play.AmbientPlayHistoryManager;
import com.android.settings.custom.ambient.history.utils.TimeDateUtils;

import com.android.settings.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AmbientPlayHistory extends PreferenceActivity implements Preference.OnPreferenceClickListener {

    ProgressDialog dlg;
    private Map<String, List<AmbientPlayHistoryPreferenceEntry>> allSongs = new HashMap<>();
    private boolean updateRunning = false;
    private String TAG = "AmbientPlayHistory";
    private MenuItem removeAll;
    private BroadcastReceiver onSongMatch = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                return;
            }
            if (intent.getAction().equals(AmbientPlayHistoryManager.INTENT_SONG_MATCH.getAction())) {
                updateList();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.ambient_play_history);
        updateList();
    }

    private void updateList() {
        if (updateRunning) {
            return;
        }
        updateRunning = true;
        getPreferenceScreen().removeAll();
        allSongs.clear();
        dlg = new ProgressDialog(this);
        dlg.setCancelable(false);
        dlg.setMessage(getString(R.string.ambient_play_loading_data));
        dlg.show();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(1000);
                List<AmbientPlayHistoryEntry> songs = AmbientPlayHistoryManager.getSongs(AmbientPlayHistory.this);
                for (AmbientPlayHistoryEntry entry : songs) {
                    AmbientPlayHistoryPreferenceEntry preference = new AmbientPlayHistoryPreferenceEntry(entry.getSongID(), entry.geMatchTimestamp(), entry.getSongTitle(), entry.getArtistTitle(), AmbientPlayHistory.this);
                    String matchDate = preference.getFormattedMatchDate();
                    if (allSongs.containsKey(matchDate)) {
                        allSongs.get(matchDate).add(preference);
                    } else {
                        allSongs.put(matchDate, new ArrayList<AmbientPlayHistoryPreferenceEntry>());
                        allSongs.get(matchDate).add(preference);
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addIllustration();
                        Map<String, List<AmbientPlayHistoryPreferenceEntry>> treeMap = new TreeMap<>(allSongs);
                        for (String date : treeMap.keySet()) {
                            final List<AmbientPlayHistoryPreferenceEntry> songsByDate = allSongs.get(date);
                            final PreferenceCategory cat = new PreferenceCategory(AmbientPlayHistory.this);
                            String catTitle = TimeDateUtils.getDaysAgo(AmbientPlayHistory.this, songsByDate.get(0).geMatchTimestamp());
                            cat.setTitle(catTitle);
                            cat.setKey(date);
                            getPreferenceScreen().addPreference(cat);
                            for (final AmbientPlayHistoryPreferenceEntry songEntry : songsByDate) {
                                cat.addPreference(songEntry);
                                songEntry.setPreferenceCategory(cat);
                                songEntry.setOnPreferenceClickListener(AmbientPlayHistory.this);
                            }
                        }
                        dlg.dismiss();
                        updateListState();
                        updateRunning = false;
                    }
                });
            }
        });
    }

    public void onResume() {
        super.onResume();
        registerReceiver(onSongMatch, new IntentFilter(AmbientPlayHistoryManager.INTENT_SONG_MATCH.getAction()));
    }

    public void onPause() {
        super.onPause();
        unregisterReceiver(onSongMatch);
    }

    void addIllustration() {
        Preference pref = new Preference(this);
        pref.setLayoutResource(R.layout.ambient_play_illustration);
        pref.setSelectable(false);
        pref.setEnabled(false);
        getPreferenceScreen().addPreference(pref);
    }

    void updateListState() {
        if (allSongs.size() < 1) {
            removeAll.setVisible(false);
            Preference pref = new Preference(this);
            pref.setLayoutResource(R.layout.ambient_play_preference_empty_list);
            pref.setTitle(R.string.ambient_play_history_empty);
            pref.setSelectable(false);
            pref.setEnabled(true);
            getPreferenceScreen().addPreference(pref);
        }
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ambient_play_history, menu);
        removeAll = menu.getItem(0);
        removeAll.setVisible(allSongs.size() > 0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_remove_all:
                showRemoveAllDialog();
                return true;
            case R.id.action_add_to_home:
                createShortcut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showRemoveAllDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.ambient_play_history_menu_remove_all));
        builder.setMessage(getString(R.string.ambient_play_history_dialog_remove_all_confirmation));
        builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                AmbientPlayHistoryManager.deleteAll(AmbientPlayHistory.this);
                updateList();
            }
        });
        builder.setNegativeButton(getString(android.R.string.cancel), null);
        builder.show();
    }

    private void createShortcut() {
        try {
            ComponentName cmp = new ComponentName("com.android.settings", "Settings$AmbientPlayHistory");
            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
            ShortcutInfo shortcutInfo = new ShortcutInfo.Builder(AmbientPlayHistory.this, "now_playing_history")
                    .setActivity(cmp)
                    .setShortLabel(getString(R.string.ambient_play_history))
                    .setIcon(Icon.createWithResource(AmbientPlayHistory.this, R.drawable.ambient_play_launcher))
                    .build();
            shortcutManager.requestPinShortcut(shortcutInfo, null);
        } catch (Exception e) {
            Log.e(TAG, "Error when creating shortcut", e);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference instanceof AmbientPlayHistoryPreferenceEntry) {
            final AmbientPlayHistoryPreferenceEntry entry = (AmbientPlayHistoryPreferenceEntry) preference;
            final AmbientPlayHistoryDialogEntry dialog = new AmbientPlayHistoryDialogEntry(this, entry);
            dialog.getRemoveButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    try {
                        AmbientPlayHistoryManager.deleteSong(entry.getSongID(), AmbientPlayHistory.this);
                        allSongs.get(entry.getPreferenceCategory().getKey()).remove(entry);
                        entry.removePreference();
                        if (allSongs.get(entry.getPreferenceCategory().getKey()).size() < 1) {
                            allSongs.remove(entry.getPreferenceCategory().getKey());
                            getPreferenceScreen().removePreference(entry.getPreferenceCategory());
                        }
                    } catch (Exception ignored) {
                    }
                    updateListState();
                }
            });
            dialog.show();
        }
        return false;
    }
}
