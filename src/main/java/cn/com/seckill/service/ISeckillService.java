package cn.com.seckill.service;

import cn.com.seckill.common.response.SeckillInfoResponse;

/**  
* <p>Title: ISeckillService</p>  
* <p>Description: 秒杀相关方法</p>  
* @author Ziang Xu
*/
public interface ISeckillService {

	/**
	 * 秒杀处理主要逻辑
	 * <p>Title: startSeckill</p>  
	 * <p>Description: </p>  
	 * @param stallActivityId
	 * @param purchaseNum
	 * @param openId
	 * @param formId
	 * @param addressId
	 * @return
	 */
	public SeckillInfoResponse startSeckill(int stallActivityId, int purchaseNum, String openId, String formId,
			long addressId, String shareCode, String shareSource, String userCode );
	
	/**
	 * 判断秒杀活动是否已经开始
	 * @param stallActivityId
	 * @return
	 */
	public boolean checkStartSeckill(int stallActivityId);
	
}
