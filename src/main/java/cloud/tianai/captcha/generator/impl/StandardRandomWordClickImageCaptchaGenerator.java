package cloud.tianai.captcha.generator.impl;

import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.common.util.FontUtils;
import cloud.tianai.captcha.generator.common.model.dto.ClickImageCheckDefinition;
import cloud.tianai.captcha.generator.common.model.dto.GenerateParam;
import cloud.tianai.captcha.generator.common.model.dto.ImageCaptchaInfo;
import cloud.tianai.captcha.generator.common.util.CaptchaImageUtils;
import cloud.tianai.captcha.resource.ImageCaptchaResourceManager;
import cloud.tianai.captcha.resource.ResourceStore;
import cloud.tianai.captcha.resource.common.model.dto.Resource;
import cloud.tianai.captcha.resource.impl.provider.ClassPathResourceProvider;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import sun.font.FontDesignMetrics;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @Author: 天爱有情
 * @date 2022/4/27 11:46
 * @Description 点选验证码
 */
public class StandardRandomWordClickImageCaptchaGenerator extends AbstractClickImageCaptchaGenerator {

    /** 字体包. */
    @Getter
    @Setter
    protected Font font;
    @Getter
    @Setter
    protected FontDesignMetrics metrics;
    @Getter
    @Setter
    protected Integer clickImgWidth = 80;
    @Getter
    @Setter
    protected Integer clickImgHeight = 80;
    @Getter
    @Setter
    protected int tipImageInterferenceLineNum = 2;
    @Getter
    @Setter
    protected int tipImageInterferencePointNum = 5;

    @SneakyThrows
    public StandardRandomWordClickImageCaptchaGenerator(ImageCaptchaResourceManager imageCaptchaResourceManager, boolean initDefaultResource) {
        super(imageCaptchaResourceManager, initDefaultResource);
        this.imageCaptchaResourceManager = imageCaptchaResourceManager;

    }

    @Override
    @SneakyThrows({IOException.class, FontFormatException.class})
    protected void doInit() {
        if (this.font == null) {
            // 使用默认字体
            Resource fontResource = new Resource(null, "META-INF/fonts/SIMSUN.TTC");
            InputStream inputStream = new ClassPathResourceProvider().doGetResourceInputStream(fontResource);
            Font font = Font.createFont(Font.TRUETYPE_FONT, inputStream);
            this.font = font.deriveFont(Font.BOLD, 70);
        }
        this.metrics = FontDesignMetrics.getMetrics(font);
        if (initDefaultResource) {
            initDefaultResource();
        }
    }

    public StandardRandomWordClickImageCaptchaGenerator(ImageCaptchaResourceManager imageCaptchaResourceManager,
                                                        boolean initDefaultResource,
                                                        Font font) {
        super(imageCaptchaResourceManager, initDefaultResource);
        this.imageCaptchaResourceManager = imageCaptchaResourceManager;
        this.font = font;
    }


    public void initDefaultResource() {
        ResourceStore resourceStore = imageCaptchaResourceManager.getResourceStore();
        // 添加一些系统的资源文件
        resourceStore.addResource(CaptchaTypeConstant.WORD_IMAGE_CLICK, new Resource(ClassPathResourceProvider.NAME, StandardSliderImageCaptchaGenerator.DEFAULT_SLIDER_IMAGE_RESOURCE_PATH.concat("/1.jpg")));
    }

    @Override
    public ImgWrapper genTipImage(List<ClickImageCheckDefinition> imageCheckDefinitions) {
        String tips = imageCheckDefinitions.stream().map(ClickImageCheckDefinition::getTip).collect(Collectors.joining());
        // 生成随机颜色
        int fontWidth = metrics.stringWidth(tips);
        int width = fontWidth + 5;
        int height = metrics.getHeight() + 5;
        float left = (width - fontWidth) / 2f;
        float top = 5 / 2f + metrics.getAscent();
        BufferedImage bufferedImage = CaptchaImageUtils.genSimpleImgCaptcha(tips,
                font, metrics, width, height, left, top, tipImageInterferenceLineNum, tipImageInterferencePointNum);
        return new ImgWrapper(bufferedImage, tips);
    }

    @Override
    public ImgWrapper randomGetClickImg() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        // 随机文字
        String randomWord = FontUtils.getRandomChar(random);
        // 随机颜色
        Color randomColor = CaptchaImageUtils.getRandomColor(random);
        // 随机角度
        int randomDeg = ThreadLocalRandom.current().nextInt(0, 85);
        BufferedImage fontImage = CaptchaImageUtils.drawWordImg(randomColor,
                randomWord,
                font,
                this.metrics,
                clickImgWidth,
                clickImgHeight,
                randomDeg);
        return new ImgWrapper(fontImage, randomWord);
    }


    @Override
    public ImageCaptchaInfo wrapClickImageCaptchaInfo(GenerateParam param, BufferedImage bgImage,
                                                      BufferedImage tipImage,
                                                      List<ClickImageCheckDefinition> checkClickImageCheckDefinitionList) {
        ImageCaptchaInfo clickImageCaptchaInfo = new ImageCaptchaInfo();
        clickImageCaptchaInfo.setBackgroundImage(transform(bgImage, param.getBackgroundFormatName()));
        clickImageCaptchaInfo.setSliderImage(transform(tipImage, param.getSliderFormatName()));
        clickImageCaptchaInfo.setBgImageWidth(bgImage.getWidth());
        clickImageCaptchaInfo.setBgImageHeight(bgImage.getHeight());
        clickImageCaptchaInfo.setSliderImageWidth(tipImage.getWidth());
        clickImageCaptchaInfo.setSliderImageHeight(tipImage.getHeight());
        clickImageCaptchaInfo.setRandomX(null);
        clickImageCaptchaInfo.setTolerant(null);
        clickImageCaptchaInfo.setType(CaptchaTypeConstant.WORD_IMAGE_CLICK);
        clickImageCaptchaInfo.setExpand(checkClickImageCheckDefinitionList);
        return clickImageCaptchaInfo;
    }

}
