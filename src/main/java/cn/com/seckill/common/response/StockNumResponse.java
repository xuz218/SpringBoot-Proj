package cn.com.seckill.common.response;

/**
 * 
 * 活动库存
 * @author Ziang Xu
 *
 */
public class StockNumResponse extends BaseResponse {
	
	private Long stockNum;
	
	private Long realStockNum;

	public Long getStockNum() {
		return stockNum;
	}

	public void setStockNum(Long stockNum) {
		this.stockNum = stockNum;
	}

	public Long getRealStockNum() {
		return realStockNum;
	}

	public void setRealStockNum(Long realStockNum) {
		this.realStockNum = realStockNum;
	}

}
