package com.liuh.gallerybackend.utils;

/**
 * @Author LiuH
 * @Date 2025/10/4 下午8:06
 * @PackageName com.liuh.gallerybackend.utils
 * @ClassName ColorTransformUtils
 * @Version
 * @Description 将返回的色调信息转化为标准格式
 * <p>
 * 在腾讯cos中转换的标准格式, 遇到 00 会转变为 0
 */

@SuppressWarnings("all")
public class ColorTransformUtils {

    private ColorTransformUtils() {

    }

    /**
     * 首先我们应该先摘下0x这个前缀，
     * 然后对剩余的十六进制位单独处理，r，g，b三个两位十六进制互相独立，
     * 我们分成三组依次处理，判断截取后的字符串长度，
     * 如果长度为3，直接返回0x000000，
     * 如果长度为4位或者5位，
     * 如果开头为0，那么第一组的结果就为00，
     * 如果不为0，那么第一组的结果应该为第一个十六进制为拼接上第二个十六进制位，
     * 其他两组也是如此。如果长度为6位，则拼接上0x直接返回。
     *
     * @param color
     * @return
     */
    public static String expandHexColor(String color) {
        // 去除可能存在的0x前缀
        String input = color.startsWith("0x") ? color.substring(2) : color;
        int length = input.length();
        // 长度为3直接返回
        if (length == 3) {
            return "0x000000";
        }
        int index = 0;
        StringBuilder expanded = new StringBuilder();

        // 处理三个颜色分量
        for (int i = 0; i < 3; i++) {
            char current = input.charAt(index);
            System.out.println(expanded);
            System.out.println(index);
            System.out.println(length);
            if (current == '0') {
                // 当前分量是00的情况
                expanded.append("00");
                index++;
            } else {
                // 正常分量处理（可能包含补零）
                if (index + 1 < length) {
                    expanded.append(current).append(input.charAt(index + 1));
                    index += 2;
                } else {
                    // 最后一个字符单独处理，补零
                    expanded.append(current).append('0');
                    index += 2;
                }
            }
        }

        return "0x" + expanded.toString();
    }

    public static void main(String[] args) {
        System.out.println(expandHexColor("0x5002"));
    }
}
