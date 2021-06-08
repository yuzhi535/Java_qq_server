package com.Yuxi;

import java.io.Serializable;

/**
 * 定义传输的数据格式
 *
 * param data_size  data size
 * param group      users
 * param total_size total size
 * param index      the index of the data
 * param type       text or img. 1 is text and 2 is img
 */
public class User implements Serializable {
    private static final long serialVersionUID = 2709425275741743919L;
    private String user_name;
    private String passwd;
    private int data_size;
    private String group;
    private byte[] data;
    private int total_size;
    private int index;
    private int type;      // 1 text   2 img

    User(String _user_name, String _passwd, int _index, int _type, int size, String users, byte[] _data, int _total_size) {
        user_name = _user_name;
        passwd = _passwd;
        data = _data;
        type = _type;
        data_size = size;
        group = users;
        total_size = _total_size;
        index = _index;
    }

    int getData_size() {
        return data_size;
    }

    String getGroup() {
        return group;
    }

    byte[] getData() {
        return data;
    }

    int getTotal_size() {
        return total_size;
    }

    int getIndex() {
        return index;
    }

    int getType() {
        return type;
    }

    String getUser_name() {
        return user_name;
    }
}

