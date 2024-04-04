import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.img.gif.AnimatedGifEncoder;
import cn.hutool.core.io.FileUtil;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author: hellodk
 * @description main service class
 * - 把 telegram 导出的 jpg/jpeg/png 静态表情图片转换成微信能够导入的 `.gif` 文件
 * - 输入一个文件夹路径，转换该文件夹下的所有 sticker 图片（不会递归查询子文件夹）
 * - 输出一个文件夹路径，保存所有的 `.gif` 文件，可以全选拖动到微信聊天页，然后发送，等发送成功就可以「Add Sticker」了
 * @date: 2022/1/24 11:26
 */

public class MainService {

    private static final Logger logger = Logger.getLogger(MainService.class.getName());

    public static void main(String[] args) throws Exception {
        int length = args.length;
        if (length != 2) {
            logger.warning("wrong parameters! usage: java -jar app.jar IMG_FOLDER OUTPUT_FOLDER");
            return;
        }

        MainService mainService = new MainService();
        mainService.jpg2gif(args[0], args[1]);

    }

    public void jpg2gif(String imgFolder, String outputFolder) throws Exception {
        Path outputPath = Paths.get(outputFolder);
        if (!outputPath.toFile().exists()) {
            logger.warning("output folder not exist, please make directory first!");
            return;
        }

        Path imgPath = Paths.get(imgFolder);
        if (!imgPath.toFile().exists()) {
            logger.warning("image folder not exist, please check your input.");
            return;
        }


        List<File> fileList = new ArrayList<>();
        processDirectory(imgPath.toFile(), fileList);

        if (fileList.isEmpty()) {
            logger.warning("No image files found in the specified folder.");
            return;
        }


        for (int i = 0; i < fileList.size(); i++) {
                File tempFile = fileList.get(i);
                String tempFileName = tempFile.getName();

                if (!tempFile.exists()) {
                    logger.warning("File does not exist: " + tempFile.getAbsolutePath());
                    continue; // Skip to the next file
                }


                BufferedImage src = ImageIO.read(tempFile);
                if (src == null) {
                        logger.warning("Failed to read image: " + tempFile.getAbsolutePath());
                        continue; // Skip to the next file
                }

                AnimatedGifEncoder gifEncoder = new AnimatedGifEncoder();

                // 循环模式,0代表无限循环
                gifEncoder.setRepeat(0);

                // 保留原图的 alpha channel 信息，设置 gif 背景图为透明
                /**
                * new rgba color
                * red
                * green
                * blue
                * alpha
                * 255,255,255 是白色 效果差强人意(勉强使人满意)
                */
                int lastDot = tempFileName.lastIndexOf('.');
                String newFileName = tempFileName.substring(0, lastDot) + ".gif";

                gifEncoder.setTransparent(new Color(255, 255, 255, src.getColorModel().getTransparency()), true);

                logger.info("current sequence is " + (i + 1) + " and output file name is " + newFileName);

                gifEncoder.start(outputPath + "/" + newFileName);
                gifEncoder.setDelay(100);

                gifEncoder.addFrame(src);
                gifEncoder.setDelay(100);

                gifEncoder.finish();

        }
    }


    private static void processDirectory(File directory, List<File> fileList) {
        File[] files = directory.listFiles();
        if (files != null) {

            for (File file : files) {

                if (file.isDirectory()) {
                    processDirectory(file, fileList);
                } else if (isImageFile(file)) {
                    fileList.add(file);
                }

            }

        }
    }

    private static boolean isImageFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png");
    }

}
