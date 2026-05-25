package com.example.autoteach;

//ממשק שבעזרתו אני מבצע ASYNC לכל הAPI CALLS שלי כל פעם שאני קורא לאפליקציה חיצונית API אני מקבל את הCALLBACK ובכדי להעביר אותו בין מחלקות ופונקציות שונות כך שהקוד יחכה לתשובה מהAPI אני משתמש בממשק הזה


public interface Listener {
    void onSuccess(String result);
    void onFailure(String errorMessage);
}
