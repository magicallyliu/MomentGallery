package com.liuh.gallerybackend.api.aliyunAi;


import cn.hutool.core.util.ObjUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.liuh.gallerybackend.api.aliyunAi.model.CreateOutPaintingTaskRequest;
import com.liuh.gallerybackend.api.aliyunAi.model.CreateOutPaintingTaskResponse;
import com.liuh.gallerybackend.api.aliyunAi.model.GetOutPaintingTaskResponse;
import com.liuh.gallerybackend.exception.BusinessException;
import com.liuh.gallerybackend.exception.ErrorCode;
import com.liuh.gallerybackend.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Author LiuH
 * @Date 2025/10/5 下午9:14
 * @PackageName com.liuh.gallerybackend.api.aliyunAi
 * @ClassName AliYunApi
 * @Version
 * @Description 调用阿里云ai
 */

@SuppressWarnings("all")
@Slf4j
@Component
public class AliYunApi {

    /**
     * 读取配置文件中阿里云的秘钥
     */
    @Value("${aliYunAi.apikey}")
    private String apiKey;

    /**
     * 创建任务的地址
     * https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting
     */
    public static final String CREATE_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting";

    /**
     * 查询任务状态的地址
     * https://dashscope.aliyuncs.com/api/v1/tasks/{task_id}
     */
    public static final String GET_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/%s";

    /**
     * 创建扩图任务
     *
     * @param createOutPaintingTaskRequest
     * @return
     */
    public CreateOutPaintingTaskResponse createOutPaintingTask(CreateOutPaintingTaskRequest createOutPaintingTaskRequest) {
        //判断是否存在
        ThrowUtils.throwIf(ObjUtil.isNull(createOutPaintingTaskRequest), ErrorCode.PARAMS_ERROR, "ai请求参数不能为空");

        /**
         * 请求任务
         * curl --location --request POST 'https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting' \
         * --header "Authorization: Bearer $DASHSCOPE_API_KEY" \
         * --header 'X-DashScope-Async: enable' \
         * --header 'Content-Type: application/json' \
         * --data '{
         *     "model": "image-out-painting",
         *     "input": {
         *         "image_url": "http://xxx/image.jpg"
         *     },
         *     "parameters":{
         *         "angle": 45,
         *         "x_scale":1.5,
         *         "y_scale":1.5
         *     }
         * }'
         */
        HttpRequest request = HttpRequest.post(CREATE_OUT_PAINTING_TASK_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("X-DashScope-Async", "enable")//开启异步任务
                .header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(createOutPaintingTaskRequest));

        //处理响应
        //在执行完之后会自动释放资源，无需手动关闭
        try (HttpResponse httpResponse = request.execute()) {
            if (!httpResponse.isOk()) {
                log.error("请求失败，状态码：{}", httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI扩图失败");
            }
            //将返回的json数据转换为创建扩图任务响应对象
            CreateOutPaintingTaskResponse createOutPaintingTaskResponse = JSONUtil.toBean(httpResponse.body(), CreateOutPaintingTaskResponse.class);
            //如果code返回不为空, 则发生报错
            if (ObjUtil.isNotEmpty(createOutPaintingTaskResponse.getCode())) {
                String message = createOutPaintingTaskResponse.getMessage();
                log.error("请求异常:{}", message);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI扩图失败" + message);
            }
            //返回响应
            return createOutPaintingTaskResponse;
        }


    }

    /**
     * 查询创建的任务结果
     *
     * @param taskId
     * @return
     */
    public GetOutPaintingTaskResponse getOutPaintingTaskResponse(String taskId) {
        //判断是否存在
        ThrowUtils.throwIf(ObjUtil.isEmpty(taskId), ErrorCode.PARAMS_ERROR, "任务id不能为空");

        /**
         * 查询任务结果
         * curl -X GET https://dashscope.aliyuncs.com/api/v1/tasks/86ecf553-d340-4e21-xxxxxxxxx \
         * --header "Authorization: Bearer $DASHSCOPE_API_KEY"
         */

        //请求地址
        String url = String.format(GET_OUT_PAINTING_TASK_URL, taskId);

        try (HttpResponse httpResponse = HttpRequest.get(url)
                .header("Authorization", "Bearer " + apiKey)
                .execute()) {
            if (!httpResponse.isOk()) {
                log.error("请求失败，状态码：{}", httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取任务失败");
            }
            //返回响应
            return JSONUtil.toBean(httpResponse.body(), GetOutPaintingTaskResponse.class);
        }
    }

}



