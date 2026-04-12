package com.jcen.medpal.utils;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import net.coobird.thumbnailator.name.Rename;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * 图片处理工具类
 * 提供图片压缩、水印等功能
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
public class ImageUtils {

    // 最大宽度
    private static final int MAX_WIDTH = 1920;
    // 最大高度
    private static final int MAX_HEIGHT = 1080;
    // 图片质量 (0.0 - 1.0)
    private static final double COMPRESS_QUALITY = 0.85;
    // 水印文字
    private static final String WATERMARK_TEXT = "MedPal";
    // 支持的图片格式
    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp");

    /**
     * 检查文件是否为图片
     *
     * @param extension 文件后缀
     * @return 是否为图片
     */
    public static boolean isImage(String extension) {
        if (extension == null || extension.isEmpty()) {
            return false;
        }
        return IMAGE_EXTENSIONS.contains(extension.toLowerCase());
    }

    /**
     * 处理图片：压缩 + 添加水印
     *
     * @param inputFile  输入文件
     * @param outputFile 输出文件
     * @throws IOException IO异常
     */
    public static void processImage(File inputFile, File outputFile) throws IOException {
        BufferedImage originalImage = ImageIO.read(inputFile);
        if (originalImage == null) {
            throw new IOException("无法读取图片文件");
        }

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // 计算压缩后的尺寸
        int targetWidth = originalWidth;
        int targetHeight = originalHeight;

        if (originalWidth > MAX_WIDTH || originalHeight > MAX_HEIGHT) {
            double widthRatio = (double) MAX_WIDTH / originalWidth;
            double heightRatio = (double) MAX_HEIGHT / originalHeight;
            double ratio = Math.min(widthRatio, heightRatio);

            targetWidth = (int) (originalWidth * ratio);
            targetHeight = (int) (originalHeight * ratio);
        }

        // 添加水印
        BufferedImage watermarkedImage = addTextWatermark(originalImage, WATERMARK_TEXT);

        // 压缩并保存
        String extension = getFileExtension(outputFile.getName());
        String format = extension.equalsIgnoreCase("png") ? "png" : "jpg";

        Thumbnails.of(watermarkedImage)
                .size(targetWidth, targetHeight)
                .outputQuality(COMPRESS_QUALITY)
                .outputFormat(format)
                .toFile(outputFile);
    }

    /**
     * 处理图片流：压缩 + 添加水印
     *
     * @param inputStream 输入流
     * @param outputFile  输出文件
     * @param extension   文件后缀
     * @throws IOException IO异常
     */
    public static void processImage(InputStream inputStream, File outputFile, String extension) throws IOException {
        BufferedImage originalImage = ImageIO.read(inputStream);
        if (originalImage == null) {
            throw new IOException("无法读取图片流");
        }

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // 计算压缩后的尺寸
        int targetWidth = originalWidth;
        int targetHeight = originalHeight;

        if (originalWidth > MAX_WIDTH || originalHeight > MAX_HEIGHT) {
            double widthRatio = (double) MAX_WIDTH / originalWidth;
            double heightRatio = (double) MAX_HEIGHT / originalHeight;
            double ratio = Math.min(widthRatio, heightRatio);

            targetWidth = (int) (originalWidth * ratio);
            targetHeight = (int) (originalHeight * ratio);
        }

        // 添加水印
        BufferedImage watermarkedImage = addTextWatermark(originalImage, WATERMARK_TEXT);

        // 压缩并保存
        String format = extension.equalsIgnoreCase("png") ? "png" : "jpg";

        Thumbnails.of(watermarkedImage)
                .size(targetWidth, targetHeight)
                .outputQuality(COMPRESS_QUALITY)
                .outputFormat(format)
                .toFile(outputFile);
    }

    /**
     * 添加文字水印
     *
     * @param sourceImage 原图
     * @param watermarkText 水印文字
     * @return 带水印的图片
     */
    private static BufferedImage addTextWatermark(BufferedImage sourceImage, String watermarkText) {
        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();

        // 创建新图片
        BufferedImage watermarkedImage = new BufferedImage(width, height, sourceImage.getType());
        Graphics2D g2d = watermarkedImage.createGraphics();

        // 绘制原图
        g2d.drawImage(sourceImage, 0, 0, null);

        // 设置水印样式
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));

        // 根据图片大小动态调整字体大小
        int fontSize = Math.max(12, Math.min(width, height) / 25);
        g2d.setFont(new Font("Arial", Font.BOLD, fontSize));
        g2d.setColor(Color.WHITE);

        // 计算文字位置（右下角，留出边距）
        FontMetrics fontMetrics = g2d.getFontMetrics();
        int textWidth = fontMetrics.stringWidth(watermarkText);
        int textHeight = fontMetrics.getHeight();
        int x = width - textWidth - 20;
        int y = height - 20;

        // 绘制阴影效果
        g2d.setColor(new Color(0, 0, 0, 128));
        g2d.drawString(watermarkText, x + 2, y + 2);

        // 绘制文字
        g2d.setColor(new Color(255, 255, 255, 180));
        g2d.drawString(watermarkText, x, y);

        g2d.dispose();

        return watermarkedImage;
    }

    /**
     * 获取文件后缀
     *
     * @param filename 文件名
     * @return 文件后缀
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 获取压缩后的文件名
     *
     * @param originalFilename 原文件名
     * @return 压缩后的文件名
     */
    public static String getCompressedFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isEmpty()) {
            return "compressed.jpg";
        }

        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return originalFilename + "_compressed";
        }

        String name = originalFilename.substring(0, lastDotIndex);
        String extension = originalFilename.substring(lastDotIndex + 1);

        // 转换为jpg格式以提高压缩率
        String targetExtension = extension.equalsIgnoreCase("png") ? "png" : "jpg";
        return name + "_compressed." + targetExtension;
    }

    /**
     * 获取原始文件备份名
     *
     * @param originalFilename 原文件名
     * @return 备份文件名
     */
    public static String getOriginalBackupFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isEmpty()) {
            return "original_backup";
        }

        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return originalFilename + "_original";
        }

        String name = originalFilename.substring(0, lastDotIndex);
        String extension = originalFilename.substring(lastDotIndex);

        return name + "_original" + extension;
    }
}
