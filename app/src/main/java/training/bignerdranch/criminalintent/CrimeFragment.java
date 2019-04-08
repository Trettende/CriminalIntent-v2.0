package training.bignerdranch.criminalintent;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_CONTACT = 2;

    private Crime mCrime;
    private String mSuspectId;

    private EditText mTitleField;
    private Button mDateButton;
    private Button mTimeButton;
    private CheckBox mSolvedCheckBox;
    private Button mReportButton;
    private Button mSuspectButton;
    private Button mCallButton;

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = view.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mDateButton = view.findViewById(R.id.crime_date);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        mTimeButton = view.findViewById(R.id.crime_time);
        updateTime();
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(mCrime.getTime());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
                dialog.show(manager, DIALOG_TIME);
            }
        });

        mSolvedCheckBox = view.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mCrime.setSolved(isChecked);
            }
        });

        mReportButton = view.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
                intent = Intent.createChooser(intent, getString(R.string.send_report));
                startActivity(intent);*/

                Intent intent = ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText(getCrimeReport())
                        .setSubject(getString(R.string.crime_report_subject))
                        .setChooserTitle(getString(R.string.send_report))
                        .createChooserIntent();
                startActivity(intent);

            }
        });

        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        mSuspectButton = view.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });
        if (mCrime.getSuspect() != null) {
            mSuspectButton.setText(mCrime.getSuspect());
        }

        PackageManager manager = getActivity().getPackageManager();
        if (manager.resolveActivity(pickContact,
                PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mSuspectButton.setEnabled(false);
        }

        //final Intent intent = new Intent(Intent.ACTION_DIAL);
        mCallButton = view.findViewById(R.id.crime_call_suspect);
        mCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Uri number = Uri.parse("tel:" + mCrime.getSuspectNumber());
                intent.setData(number);
                startActivity(intent);*/

                Uri numberContactUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

                String[] queryFields = {
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                };

                String selectionClause = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";

                String[] selectionArgs = {Long.toString(mCrime.getContactId())};

                Cursor cursor = getActivity().getContentResolver()
                        .query(numberContactUri, queryFields, selectionClause, selectionArgs, null);

                if (cursor != null && cursor.getCount() > 0) {
                    try {
                    /*if (cursor.getCount() == 0) {
                        return;
                    }*/
                        cursor.moveToFirst();

                        String number = cursor.getString(0);
                        Uri numberUri = Uri.parse("tel:" + number);
                        Intent intent = new Intent(Intent.ACTION_DIAL, numberUri);
                        startActivity(intent);

                    } finally {
                        cursor.close();
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_crime:
                CrimeLab.get(getActivity()).deleteCrime(mCrime);
                getActivity().finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
        } else if (requestCode == REQUEST_TIME) {
            Date time = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
            mCrime.setTime(time);
            updateTime();
        } else if (requestCode == REQUEST_CONTACT && data != null) {
            /*Uri contactUri = data.getData();
            // Определение полей, значения которых должны быть возвращены запросом.
            String[] queryFields = new String[] {
                    ContactsContract.Contacts.DISPLAY_NAME
            };
            // Выполнение запроса - contactUri здесь выполняет функции условия "where"
            Cursor cursor = getActivity().getContentResolver()
                    .query(contactUri, queryFields, null, null, null);
            try {
                if (cursor.getCount() == 0) {
                    return;
                }
                // Извлечение первого столбца данных - имени подозреваемого.
                cursor.moveToFirst();
                String suspect = cursor.getString(0);
                mCrime.setSuspect(suspect);
                mSuspectButton.setText(suspect);
            } finally {
                cursor.close();
            }*/

            /*String suspect = getSuspect(data);
            mCrime.setSuspect(suspect);
            mSuspectButton.setText(suspect);

            String suspectNumber = getSuspectNumber(mSuspectId);
            mCrime.setSuspectNumber(suspectNumber);*/

            Uri contactUri = data.getData();
            // Определение полей, значения которых должны быть возвращены запросом.
            String[] queryFields = new String[] {
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts._ID
            };
            // Выполнение запроса - contactUri здесь выполняет функции условия "where"
            Cursor cursor = getActivity().getContentResolver()
                    .query(contactUri, queryFields, null, null, null);
            try {
                if (cursor.getCount() == 0) {
                    return;
                }
                // Извлечение первого столбца данных - имени подозреваемого.
                cursor.moveToFirst();
                String suspect = cursor.getString(0);
                long contactId = cursor.getLong(1);

                mCrime.setSuspect(suspect);
                mCrime.setContactId(contactId);

                mSuspectButton.setText(suspect);
            } finally {
                cursor.close();
            }
        }
    }

    private void updateDate() {
        //mDateButton.setText(mCrime.getDate().toString());
        mDateButton.setText(DateFormat.getDateInstance(DateFormat.FULL, Locale.ENGLISH)
                .format(mCrime.getDate()));
    }

    private void updateTime() {
        mTimeButton.setText(DateFormat.getTimeInstance(DateFormat.LONG, Locale.ENGLISH)
                .format(mCrime.getTime()));
    }

    private String getCrimeReport() {
        String solvedString = null;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateString = DateFormat.getDateInstance(DateFormat.FULL, Locale.ENGLISH)
                .format(mCrime.getDate());

        String timeString = DateFormat.getTimeInstance(DateFormat.LONG, Locale.ENGLISH)
                .format(mCrime.getTime());

        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        String report = getString(R.string.crime_report,
                mCrime.getTitle(), dateString, timeString, solvedString, suspect);

        return report;
    }

    /*private void updateSuspectNumber() {
        String suspectNumber = getSuspectNumber(mSuspectId);
        mCrime.setSuspectNumber(suspectNumber);
    }*/

    private String getSuspect(Intent data) {
        Uri contactUri = data.getData();

        // Определение полей, значения которых должны быть возвращены запросом.
        String[] queryFields = new String[] {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME
        };

        // Выполнение запроса - contactUri здесь выполняет функции условия "where"
        Cursor cursor = getActivity().getContentResolver()
                .query(contactUri, queryFields, null, null, null);
        try {
            if (cursor.getCount() == 0) {
                return null;
            }
            // Извлечение первого столбца данных - имени подозреваемого.
            cursor.moveToFirst();
            mSuspectId = cursor.getString(0);
            String suspect = cursor.getString(1);
            return suspect;
        } finally {
            cursor.close();
        }
    }

    private String getSuspectNumber(String suspectId) {
        String suspectNumber = null;

        Uri numberContactUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        String[] queryFields = new String[] {
                ContactsContract.Data.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.TYPE,
        };

        String selectionClause = ContactsContract.Data.CONTACT_ID + " = ?";

        String[] selectionArgs = {""};
        selectionArgs[0] = suspectId;

        Cursor cursor = getActivity().getContentResolver()
                .query(numberContactUri, queryFields, selectionClause, selectionArgs, null);

        try {
            if (cursor.getCount() == 0) {
                return null;
            }

            while (cursor.moveToNext()) {
                int phoneType = cursor.getInt(cursor.getColumnIndex(ContactsContract
                    .CommonDataKinds.Phone.TYPE));
                if (phoneType == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
                    suspectNumber = cursor.getString(cursor.
                            getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));
                }

            }
        } finally {
            cursor.close();
        }

        return suspectNumber;
    }

}
