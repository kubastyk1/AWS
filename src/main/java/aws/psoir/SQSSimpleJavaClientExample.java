package aws.psoir;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import com.amazonaws.services.s3.model.S3Object;

public class SQSSimpleJavaClientExample {
	
	private static AmazonSQS sqs;
	private static AmazonS3 s3Client;
	
	public static void main(String[] args) {

		System.out.println("===============================================");
		System.out.println("=============== AWS is working ================");
		System.out.println("===============================================\n");
		
		init();

		try {
			while(true) {
				SQSHelper sqsHelper = new SQSHelper(sqs);
				S3Helper s3Helper = new S3Helper(s3Client);
							
				List<String> pictureUrlList = sqsHelper.receivePictureUrlsFromSQS();
				List<S3Object> s3Objects = s3Helper.getObjects(pictureUrlList);
				
				List<Picture> pictureList = new ArrayList<Picture>();
				
				for (int i = 0; i < pictureUrlList.size(); i++) {
					Picture picture = new Picture(s3Objects.get(i).getObjectContent(), pictureUrlList.get(i));
					picture.transferPicture();
					pictureList.add(picture);
				}
				
				s3Helper.uploadFiles(pictureList);
			}
		} catch (final AmazonServiceException ase) {
			System.out.println(
					"Caught an AmazonServiceException, which means " + "your request made it to Amazon SQS, but was "
							+ "rejected with an error response for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (final AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means "
					+ "the client encountered a serious internal problem while "
					+ "trying to communicate with Amazon SQS, such as not " + "being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	private static void init() {
		sqs = AmazonSQSClientBuilder.standard().build();
		s3Client = AmazonS3ClientBuilder.standard().build();
	}
}