package training.bignerdranch.criminalintent;

import android.content.Context;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CrimeLab {
    private static CrimeLab sCrimeLab;

    //private Map<UUID, Crime> mCrimes;
    private List<Crime> mCrimes;


    public static CrimeLab get(Context context) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    private CrimeLab(Context context) {
        //mCrimes = new LinkedHashMap<>();
        mCrimes = new ArrayList<>();
    }

    public void addCrime(Crime crime) {
        //mCrimes.put(crime.getId(), crime);
        mCrimes.add(crime);
    }

    public void deleteCrime(Crime crime) {
        mCrimes.remove(crime);
    }

    public List<Crime> getCrimes() {
        //return new ArrayList<>(mCrimes.values());
        return mCrimes;
    }

    public Crime getCrime(UUID id) {
        //return mCrimes.get(id);
        for (Crime crime : mCrimes) {
            if (crime.getId().equals(id)) {
                return crime;
            }
        }
        return null;
    }
}
