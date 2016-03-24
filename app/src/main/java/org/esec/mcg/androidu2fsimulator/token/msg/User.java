package org.esec.mcg.androidu2fsimulator.token.msg;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yz on 2016/3/23.
 */
public class User implements Parcelable {
    public byte name;
    public int age;
    public byte[] array;

    // 必须要创建一个名叫CREATOR的常量。
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }
        //重写createFromParcel方法，创建并返回一个获得了数据的user对象
        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public String toString() {
        return name + ":" + age;
    }

    // 无参数构造器方法，供外界创建类的实例时调用
    public User(byte name, int age, byte[] array) {
        this.name = name;
        this.age = age;
        this.array = array;
    }

    // 带参构造器方法私用化，本构造器仅供类的方法createFromParcel调用
    private User(Parcel source) {
        name = source.readByte();
        age = source.readInt();
        source.readByteArray(array);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // 将对象中的属性保存至目标对象dest中
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(name);
        dest.writeInt(age);
        dest.writeByteArray(array);
    }

//省略gettertter
}
