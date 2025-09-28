package com.liuh.gallerybackend.model.dto.space;

/**
 * @Author LiuH
 * @Date 2025/9/27 上午10:54
 * @PackageName com.liuh.gallerybackend.model.dto.space
 * @ClassName SpaceLevel
 * @Version
 * @Description 空间级别 用于返回个个空间级别等等额度
 */

import lombok.AllArgsConstructor;
import lombok.Data;

@SuppressWarnings("all")
@Data
//@AllArgsConstructor 生成一个构造所有变量的构造器
@AllArgsConstructor
public class SpaceLevel {

  /**
   *  空间级别：0-普通版 1-专业版 2-旗舰版
   */
  private int value;

  /**
   *  空间级别名称
   */
  private String text;

  /**
   * 空间图片的最大总大小
   */
  private Long maxSize;

  /**
   * 空间图片的最大数量
   */
  private Long maxCount;
}

