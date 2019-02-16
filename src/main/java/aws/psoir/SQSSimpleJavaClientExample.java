package aws.psoir;

/*
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  https://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.SdkClientException;

import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

/**
 * This sample demonstrates how to make basic requests to Amazon SQS using the
 * AWS SDK for Java.
 * <p>
 * Prerequisites: You must have a valid Amazon Web Services developer account,
 * and be signed up to use Amazon SQS. For more information about Amazon SQS,
 * see https://aws.amazon.com/sqs
 * <p>
 * Make sure that your credentials are located in ~/.aws/credentials
 */
public class SQSSimpleJavaClientExample {
	
	private static BufferedImage picture;
	private static BasicAWSCredentials creds = new BasicAWSCredentials("key", "security-key");
	
	
	public static void main(String[] args) {
		/*
		 * Create a new instance of the builder with all defaults (credentials and
		 * region) set automatically. For more information, see Creating Service Clients
		 * in the AWS SDK for Java Developer Guide.
		 */
		
		final AmazonSQS sqs = AmazonSQSClientBuilder.standard()
			.withRegion("eu-central-1")
			.withCredentials(new AWSStaticCredentialsProvider(creds))
			.build();
				
		System.out.println("===============================================");
		System.out.println("Getting Started with Amazon SQS Standard Queues");
		System.out.println("===============================================\n");

		try {
			while(true) {
				String pictureName = "";
	
				List<String> queueList = sqs.listQueues().getQueueUrls();				
				String myQueueUrl = queueList.get(0);
				
				System.out.println("Receiving messages from MyQueue.\n");
				
				final SetQueueAttributesRequest setQueueAttributesRequest = new SetQueueAttributesRequest()
				        .withQueueUrl(myQueueUrl)
				        .addAttributesEntry("ReceiveMessageWaitTimeSeconds", "20");
				sqs.setQueueAttributes(setQueueAttributesRequest);
				
				ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()
				        .withQueueUrl(myQueueUrl)
				        .withWaitTimeSeconds(20);				
				
				//final ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(myQueueUrl);
				final List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
				for (final Message message : messages) {
					System.out.println("Message");
					System.out.println("  MessageId:     " + message.getMessageId());
					System.out.println("  ReceiptHandle: " + message.getReceiptHandle());
					System.out.println("  MD5OfBody:     " + message.getMD5OfBody());
					System.out.println("  Body:          " + message.getBody());
					
					
					pictureName = message.getBody();
								
					if (pictureName != "") {
						getObject(pictureName);
						uploadFile(pictureName);					
					}
	
					// Delete the message.
					System.out.println("Deleting a message.\n");
					final String messageReceiptHandle = messages.get(0).getReceiptHandle();
					sqs.deleteMessage(new DeleteMessageRequest(myQueueUrl, messageReceiptHandle));
				}
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
		}
	}
	
	public static void uploadFile(String pictureName) {
		String clientRegion = "eu-central-1";
        String bucketName = "psoir-bucket";
        String fileObjKeyName = "modified_pic/" + pictureName;
        String fileName = "modified_pic/" + pictureName;
        
		File file = new File("picture.jpeg");

        try {
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(clientRegion)
                    .withCredentials(new AWSStaticCredentialsProvider(creds))
                    .build();

		/*	ByteArrayOutputStream os = new ByteArrayOutputStream();
			ImageIO.write(picture, "jpeg", os);
			InputStream is = new ByteArrayInputStream(os.toByteArray());

			ObjectMetadata metadata = new ObjectMetadata();
			PutObjectRequest request = new PutObjectRequest(bucketName, fileObjKeyName, is, metadata);*/
			
		//	File file = new File("picture.jpeg");
			ImageIO.write(picture, "jpeg", file);
            PutObjectRequest request = new PutObjectRequest(bucketName, fileObjKeyName, file);			
            
            s3Client.putObject(request);
			System.out.println("File was uploaded to S3");

        }
        catch(AmazonServiceException e) {
            e.printStackTrace();
        }
        catch(SdkClientException e) {
            e.printStackTrace();
		} 
        catch (IOException e) {
			e.printStackTrace();
		} 
        finally {
           file.delete();
		}
	}
	
	public static void getObject(String pictureName) {
        String clientRegion = "eu-central-1";
        String bucketName = "psoir-bucket";
        String key = "pic/" + pictureName;

        S3Object fullObject = null;
        try {
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(clientRegion)
        			.withCredentials(new AWSStaticCredentialsProvider(creds))
                    .build();

            // Get an object and print its contents.
            System.out.println("Downloading an object");
            fullObject = s3Client.getObject(new GetObjectRequest(bucketName, key));

            picture = getImage(fullObject.getObjectContent());

        }
        catch(AmazonServiceException e) {
            e.printStackTrace();
        }
        catch(SdkClientException e) {
            e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static BufferedImage getImage(InputStream input) throws IOException {
		BufferedImage myPicture = ImageIO.read(input);
		
		AffineTransform tx = new AffineTransform();
		tx.rotate(0.5, myPicture.getWidth() / 2, myPicture.getHeight() / 2);

		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
		myPicture = op.filter(myPicture, null);
		
		return myPicture;
	}

}//https://175745531068.signin.aws.amazon.com/console-