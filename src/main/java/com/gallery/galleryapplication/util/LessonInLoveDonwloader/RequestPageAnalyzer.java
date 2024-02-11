package com.gallery.galleryapplication.util.LessonInLoveDonwloader;

import com.gallery.galleryapplication.models.Author;
import com.gallery.galleryapplication.models.FanArtImage;
import com.gallery.galleryapplication.models.Image;
import com.gallery.galleryapplication.models.Tag;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@PropertySource("classpath:setting.properties")
public class RequestPageAnalyzer {
    public static final String CURRENT_DIRECTORY = System.getProperty("user.dir");
    @Getter
    WebsiteLoginService loginService;
    @Value("${website.configPath}")
    String pathToConfig;
    @Value("${website.request}")
    String requestLink;
    List<Image> images;
    List<List<String>> bannedTags;

    @Autowired
    public RequestPageAnalyzer(WebsiteLoginService loginService) {
        this.loginService = loginService;
        images = new ArrayList<>();
        bannedTags = new ArrayList<>();
        bannedTags.add(Arrays.stream("loli,nsfw".split(",")).toList());
        bannedTags.add(Arrays.stream("shota,nsfw".split(",")).toList());
    }

    private int getPageLimit() throws IOException {
        HtmlPage numberPagesNeedet = loginService.getWebClient().getPage(requestLink + 1);
        Document numberPagesNeedet1 = Jsoup.parse(numberPagesNeedet.asXml());
        String value = numberPagesNeedet1.select("div.container > div.message").first().ownText();
        int rowImagesCount = getIntegerValueFromString(value);
        int pageLimit;
        if ((rowImagesCount / 20.0) != (int) (rowImagesCount / 20.0)) {
            pageLimit = (rowImagesCount / 20) + 1;
        } else {
            pageLimit = (rowImagesCount / 20);
        }
        return pageLimit;
    }


    public void analyzeRequestPages() throws IOException {
        int pageLimit = getPageLimit();
        for (int j = 1; j <= pageLimit; j++) {
            Elements elements = getElements(j);
            List<Image> requestImageModels = null;
            try {
                requestImageModels = elementsImageParser(elements);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            images.addAll(requestImageModels);
        }

    }

    private List<Image> elementsImageParser(Elements elements) throws ParseException {
        List<Image> temp = new ArrayList<>();
        itemsLoop:
        for (int i = 1; i <= elements.size() - 2; i++) {
            Image image = getImagesFromElement(elements.get(i));
            if (image == null) {
                continue;
            }
            temp.add(image);
        }
        return temp;
    }

    private int getIntegerValueFromString(String input) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(input);
        matcher.find();
        return Integer.parseInt(matcher.group());
    }

    public String downloadImage(String urlE, String mediaId) {

        try {
            WebClient webClient = loginService.getWebClient();
            String url = "https://lessonsinlovegame.com" + urlE;
            try (InputStream in = webClient.getPage(url).getWebResponse().getContentAsStream();
                 FileOutputStream fos = new FileOutputStream(CURRENT_DIRECTORY + File.separator + "image" + File.separator + mediaId + ".png")) {
                int len;
                byte[] buffer = new byte[4096]; // Experiment with different buffer sizes
                while ((len = in.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
            }
        } catch (IOException e) {
            LoggerFactory.getLogger(this.getClass()).warn("Error while loading image", e);
        }
        return CURRENT_DIRECTORY + File.separator + "image" + File.separator + mediaId + ".png";
    }


    private Elements getElements(int j) throws IOException {
        HtmlPage requestPage = loginService.getWebClient().getPage(requestLink + j);
        Document document = Jsoup.parse(requestPage.asXml());
        return document.select(".block-inner");
    }

    private Image getImagesFromElement(Element element) throws ParseException {
        Image image = new Image();
        String mediaId = element.child(1).attr("data-media-id");
        element = element.child(0);
        String imageUrl = element.attr("href");
        if (imageUrl.endsWith("gif.png")) {
            return null;
        }
        element = element.selectFirst("div.overlay");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String date = element.child(0).ownText();
        String tags = element.child(1).ownText();


        if (tags.equalsIgnoreCase("None yet")) {
            return null;
        }
        String authorName = element.child(2).ownText();
        Author author = new Author();
        author.setName(authorName);
        String likesString = element.child(3).child(1).ownText();
        int likes = Integer.parseInt(likesString);
        image.setPathToFileOnDisc(downloadImage(imageUrl, mediaId));
        image.setMediaId(Integer.parseInt(mediaId));
        image.setCreationDate(simpleDateFormat.parse(date));
        image.setTags(Tag.createTagsFromList(Stream.of(tags.split(",")).map(x -> x.toLowerCase().trim()).toList()));
        image.setAuthor(author);
        return image;
    }


    public List<Image> loginAndDownloadImages() {
        String currentDirectory = System.getProperty("user.dir");
        File folder = new File(currentDirectory + File.separator + "image");
        if (!folder.exists()) {
            if (folder.mkdirs()) {
                System.out.println("Folder created successfully.");
            } else {
                System.out.println("Failed to create the folder.");
            }
        } else {
            System.out.println("The folder already exists.");
        }
        try {
            if (loginService.getLoginStatus()) {
                loginService.loginToWebsite();
            }
        } catch (IOException e) {
            LoggerFactory.getLogger(this.getClass()).error("Error to logging sitee" + e.getMessage());
        }
        try {
            analyzeRequestPages();
        } catch (IOException e) {
            LoggerFactory.getLogger(this.getClass()).error("Error to logging errorInAnalyze" + e.getMessage());
        }
        return images = images.stream().distinct().collect(Collectors.toList());
    }

    @PostConstruct
    public void onStartUp() {
      /*  try {
            this.loginAndDownloadImages();
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            LoggerFactory.getLogger(this.getClass())
                    .info("All post start up tasks done");
        }*/
    }

    public List<Image> checkUpdates(List<Image> allImages) throws IOException {
        List<Image> globalNewImages = new ArrayList<>();
        if (loginService.getLoginStatus()) {
            try {
                loginService.loginToWebsite();
            } catch (IOException e) {
                LoggerFactory.getLogger(this.getClass()).error("Error while loging to site" + e.getMessage());
            }
        }
        int pageLimit = getPageLimit();
        for (int i = 0; i <= pageLimit; i++) {
            Elements elements = getElements(i);
            List<Image> newImages = new ArrayList<>();
            try {
                newImages = elementsImageParser(elements);
            } catch (ParseException e) {
                LoggerFactory.getLogger(this.getClass()).error("Something gonna wrong");
            }
            if(allImages.stream().filter(newImages::contains).findAny().isEmpty()) {
                globalNewImages.addAll(newImages);
            }else {
                globalNewImages.addAll(newImages);
                break;
            }
        }
        return globalNewImages;
    }

    public List<FanArtImage> scanImagesFromFolder() {
        File imageFolder = new File(CURRENT_DIRECTORY + File.separator + "imageFanArts");
        List<FanArtImage> fanArtImages = new ArrayList<>();
        if (imageFolder.exists() && imageFolder.isDirectory()){
            List<File> images = Arrays.stream(imageFolder.listFiles()).toList();
             fanArtImages = images.parallelStream().map(x-> {
                FanArtImage fanArtImage = new FanArtImage();
                fanArtImage.setPathToFileOnDisc(x.getPath());
                fanArtImage.setCreationDate(extractTimestampFromFilePath(fanArtImage.getPathToFileOnDisc()));
                return fanArtImage;
            }).toList();
        }
        return fanArtImages;
    }
    private Date extractTimestampFromFilePath(String filePath) {
        // Регулярное выражение для поиска даты и времени в формате "yyyy-MM-dd_HH-mm-ss"
        String regex = "(\\d{4}-\\d{2}-\\d{2})_(\\d{2}-\\d{2}-\\d{2})";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(filePath);

        if (matcher.find()) {
            String datePart = matcher.group(1);
            String timePart = matcher.group(2);

            String dateTimeString = datePart + "_" + timePart;

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            try {
                return dateFormat.parse(dateTimeString);
            } catch (ParseException e) {
                LoggerFactory.getLogger(this.getClass()).error("Error while trying parse a date", e);
            }
        }
        return null;
    }
}
