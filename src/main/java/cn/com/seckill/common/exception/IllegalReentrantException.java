package cn.com.seckill.common.exception;

/**
 * 对于不可重入的锁将抛出此异常
 *
 * @author Ziang Xu
 */
public class IllegalReentrantException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public IllegalReentrantException(Throwable cause) {
        super(cause);
    }

    public IllegalReentrantException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalReentrantException(String message) {
        super(message);
    }

    public IllegalReentrantException() {
        super();
    }
}