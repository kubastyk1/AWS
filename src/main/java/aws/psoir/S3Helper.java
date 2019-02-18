package aws.psoir;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

public class S3Helper {

	private static AmazonS3 s3Client;
	private static final String BUCKET_NAME = "psoir-bucket";
	
	public S3Helper(AmazonS3 amazonS3) {
		this.s3Client = amazonS3;
	}
	
	public List<S3Object> getObjects(List<String> pictureUrlList) {
		List<S3Object> s3Objects = new ArrayList<S3Object>();
		
		for (String pictureUrl : pictureUrlList) {
			S3Object s3Object = getObject(pictureUrl);
			s3Objects.add(s3Object);
		}
		
		return s3Objects;
	}
	
	private static S3Object getObject(String pictureName) {
        String key = "pic/" + pictureName;

        System.out.println("Downloading an object");
        S3Object fullObject = s3Client.getObject(new GetObjectRequest(BUCKET_NAME, key));
        
        return fullObject;
	}
	
	public void uploadFiles(List<Picture> pictureList) {
		for (Picture picture : pictureList) {
			uploadFile(picture);
		}
	}
	
	public static void uploadFile(Picture picture) {
        String bucketName = "psoir-bucket";
        String fileObjKeyName = "modified_pic/" + picture.getPictureName();
        
		File file = new File("picture.jpeg");

        try {
    		BufferedImage newPicture = new BufferedImage(picture.getPicture().getWidth(), picture.getPicture().getHeight(), BufferedImage.TYPE_3BYTE_BGR);
    		newPicture.setData(picture.getPicture().getData());
            
			ImageIO.write(newPicture, "jpeg", file);
            PutObjectRequest request = new PutObjectRequest(bucketName, fileObjKeyName, file);			
            
            s3Client.putObject(request);
			System.out.println("File was uploaded to S3");
        }
        catch (IOException e) {
			e.printStackTrace();
		} 
        finally {
           file.delete();
		}
	}
}
