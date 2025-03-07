package dat.exceptions;

public class DAOException extends RuntimeException
{
    private final int code;

    public DAOException(int code, String msg)
    {
        super(msg);
        this.code = code;
    }

    public DAOException(int code, String msg, Exception e)
    {
        super(msg, e);
        this.code = code;
    }

    public int getCode()
    {
        return code;
    }
}