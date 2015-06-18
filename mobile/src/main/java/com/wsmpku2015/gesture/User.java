package com.wsmpku2015.gesture;

/**
 * Created by wsm on 4/24/15.
 */
public class User {
    final static int gestNum = 30;
    final static int rep = 10;
    private int[] curNum;
    private int uid;
    public User(int id) {
        curNum = new int[gestNum];
        uid = id;
    }
    public boolean addRep(int gest) {
        if (gest > gestNum || gest <= 0)
            return false;
        curNum[gest - 1]++;
       return true;
    }
    public boolean decRep(int gest) {
        if (gest > gestNum || gest <= 0)
            return false;
        if (curNum[gest - 1] == 0)
            return false;
        curNum[gest - 1]--;
        return true;
    }
    public int getUid() {
        return uid;
    }
    public int getRep(int gest) {
        if (gest > gestNum || gest <= 0)
            return -1;
        else return curNum[gest - 1];
    }
    public int setRep(int gest, int reps) {
        if (gest > gestNum || gest <= 0)
            return -1;
        else {
            curNum[gest - 1] = reps;
            return 0;
        }
    }
}


