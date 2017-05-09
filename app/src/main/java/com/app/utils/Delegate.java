package com.app.utils;

/**
 * Created by hitasoft on 21/1/17.
 */

public class Delegate {

    private static com.app.utils.Delegate instance;

    // Global variable
    private int data;

    // Restrict the constructor from being instantiated
    private Delegate(){}

    public void setData(int d){
        this.data=d;
    }
    public int getData(){
        return this.data;
    }

    public static synchronized Delegate getInstance(){
        if(instance==null){
            instance=new Delegate();
        }
        return instance;
    }
}
