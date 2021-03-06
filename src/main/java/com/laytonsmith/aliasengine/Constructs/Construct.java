/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.Constructs;

import com.laytonsmith.aliasengine.exceptions.MarshalException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 *
 * @author layton
 */
public abstract class Construct implements Cloneable, Comparable<Construct> {

    public static final long serialVersionUID = 1L;

    public enum ConstructType {

        TOKEN, COMMAND, FUNCTION, VARIABLE, LITERAL, ARRAY, MAP, ENTRY, INT, DOUBLE, BOOLEAN, NULL, STRING, VOID, IVARIABLE, CLOSURE, LABEL
    }
    protected ConstructType ctype;
    protected String value;
    protected int line_num;
    transient protected File file;

    public ConstructType getCType() {
        return ctype;
    }

    public String getValue() {
        return value;
    }

    public int getLineNum() {
        return line_num;
    }

    public File getFile() {
        return file;
    }

    public Construct(String value, ConstructType ctype, int line_num, File file) {
        this.value = value;
        this.ctype = ctype;
        this.line_num = line_num;
        this.file = file;
    }

    public String val() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public Construct clone() throws CloneNotSupportedException {
        return (Construct) super.clone();
    }

    public static String json_encode(Construct c) throws MarshalException{
        return json_encode(c, false);
    }
    /**
     * This function takes a Construct, and turns it into a JSON value. If the construct is
     * not one of the following, a MarshalException is thrown: CArray, CBoolean, CDouble, CInt, CNull, 
     * CString, CVoid, Command. Currently unsupported, but will be in the future are: CClosure/CFunction
     * The following map is applied when encoding and decoding:
     * <table border='1'>
     * <tr><th>JSON</th><th>MScript</th></tr>
     * <tr><td>string</td><td>CString, CVoid, Command, but all are decoded into CString</td></tr>
     * <tr><td>number</td><td>CInt, CDouble, and it is decoded intelligently</td></tr>
     * <tr><td>boolean</td><td>CBoolean</td></tr>
     * <tr><td>null</td><td>CNull</td></tr>
     * <tr><td>array</td><td>CArray</td></tr>
     * <tr><td>object</td><td>A MarshalException is currently thrown, but this will eventually return a CArray</td></tr>
     * </table>
     * @param c
     * @return 
     */
//    public static String json_encode(Construct c, boolean raw) throws MarshalException {
//        if (c instanceof CString || c instanceof Command) {
//            if(raw){
//                return JSONObject.escape(c.getValue());
//            }
//            return "\"" + JSONObject.escape(c.getValue()) + "\"";
//        } else if (c instanceof CVoid) {
//            if(raw){
//                return JSONObject.escape(c.getValue());
//            }
//            return "\"\"";
//        } else if (c instanceof CInt) {
//            return Long.toString(((CInt) c).getInt());
//        } else if (c instanceof CDouble) {
//            return Double.toString(((CDouble) c).getDouble());
//        } else if (c instanceof CBoolean) {
//            if (((CBoolean) c).getBoolean()) {
//                return "true";
//            } else {
//                return "false";
//            }
//        } else if (c instanceof CNull) {
//            return "null";
//        } else if (c instanceof CArray) {
//            CArray ca = (CArray) c;
//            if (!ca.inAssociativeMode()) {
//                StringBuilder b = new StringBuilder();
//                b.append("[");
//                for (int i = 0; i < ca.size(); i++) {
//                    if (i != 0) {
//                        b.append(", ");
//                    }
//                    b.append(json_encode(ca.get(i, 0)));
//                }
//                b.append("]");
//                return b.toString();
//            } else {
//                //We treat it like an object.
//                SortedMap<String, String> map = new TreeMap<String, String>();
//                for(Construct key : ca.keySet()){
//                    map.put(json_encode(key, true), json_encode(ca.get(key, 0), true));
//                }
//                return JSONValue.toJSONString(map);
//            }
//        } else {
//            throw new MarshalException("The type of " + c.getClass().getSimpleName() + " is not currently supported", c);
//        }
//    }
    public static String json_encode(Construct c, boolean raw) throws MarshalException {
        return JSONValue.toJSONString(json_encode0(c));
    }
    
    private static Object json_encode0(Construct c) throws MarshalException{
        if (c instanceof CString || c instanceof Command) {
            return c.val();
        } else if (c instanceof CVoid) {
            return "";
        } else if (c instanceof CInt) {
            return ((CInt) c).getInt();
        } else if (c instanceof CDouble) {
            return ((CDouble) c).getDouble();
        } else if (c instanceof CBoolean) {
            return ((CBoolean) c).getBoolean();
        } else if (c instanceof CNull) {
            return null;
        } else if (c instanceof CArray) {
            CArray ca = (CArray) c;
            if (!ca.inAssociativeMode()) {
                List<Object> list = new ArrayList<Object>();
                for(int i = 0; i < ca.size(); i++){
                    list.add(json_encode0(ca.get(i, 0)));
                }
                return list;
            } else {
                Map<String, Object> map = new HashMap<String, Object>();
                for(Construct key : ca.keySet()){
                    map.put(key.val(), json_encode0(ca.get(key, 0)));
                }
                return map;
            }
        } else {
            throw new MarshalException("The type of " + c.getClass().getSimpleName() + " is not currently supported", c);
        }
    }
    /**
     * Takes a string and converts it into a 
     * @param s
     * @return 
     */
    public static Construct json_decode(String s) throws MarshalException {
        if (s.startsWith("{")) {
            //Object, for now throw an exception
            JSONObject obj = (JSONObject) JSONValue.parse(s);
            CArray ca = new CArray(0, null);
            ca.forceAssociativeMode();
            for(Object key : obj.keySet()){
                ca.set(convertJSON(key), convertJSON(obj.get(key)));
            }
            return ca;
        } else if (s.startsWith("[")) {
            //It's an array
            JSONArray array = (JSONArray) JSONValue.parse(s);
            CArray carray = new CArray(0, null);
            for (int i = 0; i < array.size(); i++) {
                carray.push(convertJSON(array.get(i)));
            }
            return carray;
        } else {
            //It's a single value, but we're gonna wrap it in an array, then deconstruct it
            s = "[" + s + "]";
            JSONArray array = (JSONArray) JSONValue.parse(s);
            Object o = array.get(0);
            return convertJSON(o);
        }
    }

    private static Construct convertJSON(Object o) throws MarshalException {
        if (o instanceof String) {
            return new CString((String) o, 0, null);
        } else if (o instanceof Number) {
            Number n = (Number) o;
            if (n.longValue() == n.doubleValue()) {
                //It's an int
                return new CInt(n.longValue(), 0, null);
            } else {
                //It's a double
                return new CDouble(n.doubleValue(), 0, null);
            }
        } else if (o instanceof Boolean) {
            return new CBoolean(((Boolean) o).booleanValue(), 0, null);
        } else if (o instanceof java.util.List) {
            java.util.List l = (java.util.List) o;
            CArray ca = new CArray(0, null);
            for (int i = 0; i < l.size(); i++) {
                ca.push(convertJSON(l.get(i)));
            }
            return ca;
        } else if (o == null) {
            return new CNull(0, null);
        } else if(o instanceof java.util.Map){
            CArray ca = new CArray(0, null);
            ca.forceAssociativeMode();
            for(Object key : ((java.util.Map)o).keySet()){
                ca.set(convertJSON(key), convertJSON(((java.util.Map)o).get(key)));
            }
            return ca;
        } else {
            throw new MarshalException(o.getClass().getSimpleName() + " are not currently supported");
        }
    }

    public int compareTo(Construct c) {
        if(this.value.contains(" ") || this.value.contains("\t") 
                || c.value.contains(" ") || c.value.contains("\t")){
            return this.value.compareTo(c.value);
        }
        try {
            Double d1 = Double.valueOf(this.value);
            Double d2 = Double.valueOf(c.value);
            return d1.compareTo(d2);
        } catch (NumberFormatException e) {
            return this.value.compareTo(c.value);
        }
    }
}
