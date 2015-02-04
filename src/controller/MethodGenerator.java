package controller;

/**
 * Created by Hadi on 2/4/2015 1:56 PM.
 */
public class MethodGenerator {

    private String mName;
    private String[] mArgs;
    private String mRet;
    private String mBody;

    public MethodGenerator(String name, String[] args, String ret, String body) {
        mName = name;
        mArgs = args;
        mRet = ret;
        mBody = body;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("public ").append(mRet).append(" ").append(mName).append("(");
        for (int i = 0; i < mArgs.length; i++) {
            sb.append(mArgs[i]);
            if (i < mArgs.length-1)
                sb.append(", ");
        }
        sb.append(") {\n");
        sb.append(mBody.replaceAll("^", "\t"));
        sb.append("\n}");
        return sb.toString();
    }
}
