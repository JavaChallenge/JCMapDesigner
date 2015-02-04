package controller;

import java.util.ArrayList;

/**
 * Created by Hadi on 2/4/2015 1:37 PM.
 */
public class ClassGenerator {

    private String mName;
    private String mParent;
    private ArrayList<String> mTypes = new ArrayList<>();
    private ArrayList<String> mFields = new ArrayList<>();
    private ArrayList<String> mDefaults = new ArrayList<>();
    private ArrayList<MethodGenerator> mMethods = new ArrayList<>();
    private boolean mHaveConstructor;

    public ClassGenerator(String className, String extend) {
        mName = toCamelCase(className);
        mParent = extend == null ? null : toCamelCase(extend);
    }

    public void setHaveConstructor(boolean haveConstructor) {
        mHaveConstructor = haveConstructor;
    }

    public void addField(String name, String type) {
        addField(name, type, "");
    }

    public void addField(String name, String type, String def) {
        mTypes.add(type);
        mFields.add(toCamelCase(name));
        mDefaults.add(def);
    }

    public void addMethod(String name, String args[], String ret, String body) {
        mMethods.add(new MethodGenerator(name, args, ret, body));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String[] keys = mFields.toArray(new String[mFields.size()]);
        String[] args = new String[keys.length];
        String[] fields = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            fields[i] = "m" + keys[i];
            args[i] = Character.toLowerCase(fields[i].charAt(1)) + fields[i].substring(2);
        }
        sb.append("public class ").append(mName).append(' ');
        if (mParent != null)
            sb.append("extends ").append(mParent).append(' ');
        sb.append("{\n\n");
        for (int i = 0; i < keys.length; i++)
            sb.append("\tprivate ").append(mTypes.get(i)).append(' ').append(fields[i]).append(';').append('\n');
        sb.append('\n');
        if (mHaveConstructor) {
            sb.append("\tpublic ").append(mName).append('(');
            for (int i = 0; i < keys.length; i++) {
                sb.append(mTypes.get(i)).append(" ").append(args[i]);
                if (i < keys.length - 1)
                    sb.append(", ");
            }
            sb.append(") {\n");
            for (int i = 0; i < keys.length; i++)
                sb.append("\t\t").append(fields[i]).append(" = ").append(args[i]).append(";\n");
            sb.append("\t}\n\t\n");
        }
        for (int i = 0; i < keys.length; i++) {
            sb.append("\tpublic void set").append(fields[i].substring(1)).append("(").append(mTypes.get(i)).append(" ").append(args[i]).append(") {\n");
            sb.append("\t\t").append(fields[i]).append(" = ").append(args[i]).append(";\n");
            sb.append("\t}\n\n");

            sb.append("\tpublic ").append(mTypes.get(i)).append(" get").append(fields[i].substring(1)).append("() {\n");
            sb.append("\t\treturn ").append(fields[i]).append(";\n");
            sb.append("\t}\n\n");
        }
        for (MethodGenerator method : mMethods) {
            String m = method.toString();
            sb.append(m.replaceAll("^", "\t")).append("\n\n");
        }
        sb.append("}");
        return sb.toString();
    }

    public static String toCamelCase(String str) {
        StringBuilder sb = new StringBuilder();
        String[] parts = str.replaceAll("[^ a-zA-Z0-9]", "").split("\\s+");
        for (String part : parts)
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1).toLowerCase());
        return sb.toString();
    }

}
