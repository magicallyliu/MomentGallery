package com.liuh.gallerybackend.model.dto.picture;

import com.liuh.gallerybackend.api.aliyunAi.model.CreateOutPaintingTaskRequest;
import lombok.Data;

/**
 * @Author LiuH
 * @Date 2025/10/6 下午3:07
 * @PackageName com.liuh.gallerybackend.model.dto.picture
 * @ClassName CreatePictureOutPaintingTaaskRequest
 * @Version
 * @Description 前端的扩图请求 -- 创建扩图任务
 */

@SuppressWarnings("all")
@Data
public class CreatePictureOutPaintingTaskRequest {

    /**
     * 图片id
     */
    private Long pictureId;

    /**
     * 扩图参数
     */
    private CreateOutPaintingTaskRequest.Parameters parameters;

    private static final long serialVersionUID = 1L;
}
