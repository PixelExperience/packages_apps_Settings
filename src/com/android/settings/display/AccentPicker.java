/*
 * Copyright (C) 2018 The Dirty Unicorns Project
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

package com.android.settings.display;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;

public class AccentPicker extends InstrumentedDialogFragment implements OnClickListener {

    private static final String TAG_ACCENT_PICKER = "accent_picker";

    private View mView;

    private IOverlayManager mOverlayManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOverlayManager = IOverlayManager.Stub.asInterface(
                ServiceManager.getService(Context.OVERLAY_SERVICE));
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mView = LayoutInflater.from(getActivity()).inflate(R.layout.accent_picker, null);

        if (mView != null) {
            initView();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(mView)
                .setNegativeButton(R.string.cancel, this)
                .setNeutralButton(R.string.theme_accent_picker_default, this)
                .setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    private void initView() {
        final ContentResolver resolver = getActivity().getContentResolver();

        Button redAccent;
        redAccent = mView.findViewById(R.id.redAccent);
        if (redAccent != null) {
            redAccent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Settings.System.putIntForUser(resolver,
                            Settings.System.ACCENT_PICKER, 1, UserHandle.USER_CURRENT);
                    dismiss();
                }
            });
        }

        Button pinkAccent;
        pinkAccent = mView.findViewById(R.id.pinkAccent);
        if (pinkAccent != null) {
            pinkAccent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Settings.System.putIntForUser(resolver,
                            Settings.System.ACCENT_PICKER, 2, UserHandle.USER_CURRENT);
                    dismiss();
                }
            });
        }

        Button purpleAccent;
        purpleAccent = mView.findViewById(R.id.purpleAccent);
        if (purpleAccent != null) {
            purpleAccent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Settings.System.putIntForUser(resolver,
                            Settings.System.ACCENT_PICKER, 3, UserHandle.USER_CURRENT);
                    dismiss();
                }
            });
        }

        Button deeppurpleAccent;
        deeppurpleAccent = mView.findViewById(R.id.deeppurpleAccent);
        if (deeppurpleAccent != null) {
            deeppurpleAccent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Settings.System.putIntForUser(resolver,
                            Settings.System.ACCENT_PICKER, 4, UserHandle.USER_CURRENT);
                    dismiss();
                }
            });
        }

        Button indigoAccent;
        indigoAccent = mView.findViewById(R.id.indigoAccent);
        if (indigoAccent != null) {
            indigoAccent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Settings.System.putIntForUser(resolver,
                            Settings.System.ACCENT_PICKER, 5, UserHandle.USER_CURRENT);
                    dismiss();
                }
            });
        }

        Button blueAccent;
        blueAccent = mView.findViewById(R.id.blueAccent);
        if (blueAccent != null) {
            blueAccent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Settings.System.putIntForUser(resolver,
                            Settings.System.ACCENT_PICKER, 6, UserHandle.USER_CURRENT);
                    dismiss();
                }
            });
        }

        Button lightblueAccent;
        lightblueAccent = mView.findViewById(R.id.lightblueAccent);
        if (lightblueAccent != null) {
            lightblueAccent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Settings.System.putIntForUser(resolver,
                            Settings.System.ACCENT_PICKER, 7, UserHandle.USER_CURRENT);
                    dismiss();
                }
            });
        }

        Button cyanAccent;
        cyanAccent = mView.findViewById(R.id.cyanAccent);
        if (cyanAccent != null) {
            cyanAccent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Settings.System.putIntForUser(resolver,
                            Settings.System.ACCENT_PICKER, 8, UserHandle.USER_CURRENT);
                    dismiss();
                }
            });
        }

        Button tealAccent;
        tealAccent = mView.findViewById(R.id.tealAccent);
        if (tealAccent != null) {
            tealAccent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Settings.System.putIntForUser(resolver,
                            Settings.System.ACCENT_PICKER, 9, UserHandle.USER_CURRENT);
                    dismiss();
                }
            });
        }

        Button greenAccent;
        greenAccent = mView.findViewById(R.id.greenAccent);
        if (greenAccent != null) {
            greenAccent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Settings.System.putIntForUser(resolver,
                            Settings.System.ACCENT_PICKER, 10, UserHandle.USER_CURRENT);
                    dismiss();
                }
            });
        }

        Button lightgreenAccent;
        lightgreenAccent = mView.findViewById(R.id.lightgreenAccent);
        if (lightgreenAccent != null) {
            lightgreenAccent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Settings.System.putIntForUser(resolver,
                            Settings.System.ACCENT_PICKER, 11, UserHandle.USER_CURRENT);
                    dismiss();
                }
            });
        }

        Button limeAccent;
        limeAccent = mView.findViewById(R.id.limeAccent);
        if (limeAccent != null) {
            limeAccent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Settings.System.putIntForUser(resolver,
                            Settings.System.ACCENT_PICKER, 12, UserHandle.USER_CURRENT);
                    dismiss();
                }
            });
        }

        Button yellowAccent;
        yellowAccent = mView.findViewById(R.id.yellowAccent);
        if (yellowAccent != null) {
            yellowAccent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Settings.System.putIntForUser(resolver,
                            Settings.System.ACCENT_PICKER, 13, UserHandle.USER_CURRENT);
                    dismiss();
                }
            });
        }

        Button amberAccent;
        amberAccent = mView.findViewById(R.id.amberAccent);
        if (amberAccent != null) {
            amberAccent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Settings.System.putIntForUser(resolver,
                            Settings.System.ACCENT_PICKER, 14, UserHandle.USER_CURRENT);
                    dismiss();
                }
            });
        }

        Button orangeAccent;
        orangeAccent = mView.findViewById(R.id.orangeAccent);
        if (orangeAccent != null) {
            orangeAccent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Settings.System.putIntForUser(resolver,
                            Settings.System.ACCENT_PICKER, 15, UserHandle.USER_CURRENT);
                    dismiss();
                }
            });
        }

        Button deeporangeAccent;
        deeporangeAccent = mView.findViewById(R.id.deeporangeAccent);
        if (deeporangeAccent != null) {
            deeporangeAccent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Settings.System.putIntForUser(resolver,
                            Settings.System.ACCENT_PICKER, 16, UserHandle.USER_CURRENT);
                    dismiss();
                }
            });
        }

        Button brownAccent;
        brownAccent = mView.findViewById(R.id.brownAccent);
        if (brownAccent != null) {
            brownAccent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Settings.System.putIntForUser(resolver,
                            Settings.System.ACCENT_PICKER, 17, UserHandle.USER_CURRENT);
                    dismiss();
                }
            });
        }

        Button greyAccent;
        greyAccent = mView.findViewById(R.id.greyAccent);
        if (greyAccent != null) {
            greyAccent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Settings.System.putIntForUser(resolver,
                            Settings.System.ACCENT_PICKER, 18, UserHandle.USER_CURRENT);
                    dismiss();
                }
            });
        }

        Button bluegreyAccent;
        bluegreyAccent = mView.findViewById(R.id.bluegreyAccent);
        if (bluegreyAccent != null) {
            bluegreyAccent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Settings.System.putIntForUser(resolver,
                            Settings.System.ACCENT_PICKER, 19, UserHandle.USER_CURRENT);
                    dismiss();
                }
            });
        }

        Button blackAccent;
        blackAccent = mView.findViewById(R.id.blackAccent);
        // Change the accent picker button depending on whether or not the dark theme is applied
        blackAccent.setBackgroundColor(getResources().getColor(
                isUsingDarkTheme() ? R.color.accent_picker_white_accent : R.color.accent_picker_dark_accent));
        blackAccent.setBackgroundTintList(getResources().getColorStateList(
                isUsingDarkTheme() ? R.color.accent_picker_white_accent : R.color.accent_picker_dark_accent));
        blackAccent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Settings.System.putIntForUser(resolver,
                        Settings.System.ACCENT_PICKER, 20, UserHandle.USER_CURRENT);
                dismiss();
            }
        });

        GridLayout gridlayout;
        int intOrientation = getResources().getConfiguration().orientation;
        gridlayout = mView.findViewById(R.id.Gridlayout);
        // Lets split this up instead of creating two different layouts
        // just so we can change the columns
        gridlayout.setColumnCount(intOrientation == Configuration.ORIENTATION_PORTRAIT ? 5 : 8);
    }

    // Check for the dark theme overlay
    private boolean isUsingDarkTheme() {
        OverlayInfo themeInfo = null;
        try {
            themeInfo = mOverlayManager.getOverlayInfo("com.android.system.theme.dark",
                    UserHandle.USER_CURRENT);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return themeInfo != null && themeInfo.isEnabled();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        ContentResolver resolver = getActivity().getContentResolver();

        if (which == AlertDialog.BUTTON_NEGATIVE) {
            dismiss();
        }
        if (which == AlertDialog.BUTTON_NEUTRAL) {
            Settings.System.putIntForUser(resolver,
                    Settings.System.ACCENT_PICKER, 0, UserHandle.USER_CURRENT);
            dismiss();
        }
    }

    public static void show(Fragment parent) {
        if (!parent.isAdded()) return;

        final AccentPicker dialog = new AccentPicker();
        dialog.setTargetFragment(parent, 0);
        dialog.show(parent.getFragmentManager(), TAG_ACCENT_PICKER);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}
