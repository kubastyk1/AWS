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
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.SdkClientException;

import java.io.File;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import javax.imageio.ImageIO;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;
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
	
	public static void main(String[] args) {
		/*
		 * Create a new instance of the builder with all defaults (credentials and
		 * region) set automatically. For more information, see Creating Service Clients
		 * in the AWS SDK for Java Developer Guide.
		 */
		final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();

		System.out.println("===============================================");
		System.out.println("Getting Started with Amazon SQS Standard Queues");
		System.out.println("===============================================\n");

		try {
			// Create a queue.
			System.out.println("Creating a new SQS queue called MyQueue.\n");
/*			final CreateQueueRequest createQueueRequest = new CreateQueueRequest("MyQueue");
			final String myQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();
*/		
			List<String> queueList = sqs.listQueues().getQueueUrls();
			// List all queues.
			System.out.println("Listing all queues in your account.\n");
			for (final String queueUrl : sqs.listQueues().getQueueUrls()) {
				System.out.println("  QueueUrl: " + queueUrl);
			}
			System.out.println();
			
			for (String myQueueUrl : queueList) {

				// Send a message.
				System.out.println("Sending a message to MyQueue.\n");
				sqs.sendMessage(new SendMessageRequest(myQueueUrl, "This is my message text."));
	
				// Receive messages.
				System.out.println("Receiving messages from MyQueue.\n");
				final ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(myQueueUrl);
				final List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
				for (final Message message : messages) {
					System.out.println("Message");
					System.out.println("  MessageId:     " + message.getMessageId());
					System.out.println("  ReceiptHandle: " + message.getReceiptHandle());
					System.out.println("  MD5OfBody:     " + message.getMD5OfBody());
					System.out.println("  Body:          " + message.getBody());
					for (final Entry<String, String> entry : message.getAttributes().entrySet()) {
						System.out.println("Attribute");
						System.out.println("  Name:  " + entry.getKey());
						System.out.println("  Value: " + entry.getValue());
						
						getObject(entry.getValue());
						uploadFile(entry.getValue());
						
					}
				}
				System.out.println();
				
				
				
	
				// Delete the message.
				System.out.println("Deleting a message.\n");
				final String messageReceiptHandle = messages.get(0).getReceiptHandle();
				sqs.deleteMessage(new DeleteMessageRequest(myQueueUrl, messageReceiptHandle));
	
				// Delete the queue.
				System.out.println("Deleting the test queue.\n");
				sqs.deleteQueue(new DeleteQueueRequest(myQueueUrl));
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
	
	public static void uploadFile(String attribute) {
		String clientRegion = "eu-central-1";
        String bucketName = "psoir-bucket";
     //   String stringObjKeyName =  objectKey;
        String fileObjKeyName = "key";
        String fileName = Integer.toString(new Random().nextInt(10000));

        try {
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(clientRegion)
                    .withCredentials(new ProfileCredentialsProvider())
                    .build();
        /*
            // Upload a text string as a new object.
            s3Client.putObject(bucketName, stringObjKeyName, "Uploaded String Object");
       */     
            
            // Upload a file as a new object with ContentType and title specified.
            PutObjectRequest request = new PutObjectRequest(bucketName, fileObjKeyName, new File(fileName));
          /*  ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("plain/text");
            metadata.addUserMetadata("x-amz-meta-title", "someTitle");
            request.setMetadata(metadata);*/
            s3Client.putObject(request);
        }
        catch(AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process 
            // it, so it returned an error response.
            e.printStackTrace();
        }
        catch(SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
        }
	}
	
	public static void getObject(String objectKey) {
        String clientRegion = "eu-central-1";
        String bucketName = "psoir-bucket";
        String key = objectKey;

        S3Object fullObject = null, objectPortion = null, headerOverrideObject = null;
        try {
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(clientRegion)
                    .withCredentials(new ProfileCredentialsProvider())
                    .build();

            // Get an object and print its contents.
            System.out.println("Downloading an object");
            fullObject = s3Client.getObject(new GetObjectRequest(bucketName, key));
            System.out.println("Content-Type: " + fullObject.getObjectMetadata().getContentType());
            System.out.println("Content: ");
            
            try {
            	picture = getImage(fullObject.getObjectContent());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
   //         displayTextInputStream(fullObject.getObjectContent());
   /*         
            // Get a range of bytes from an object and print the bytes.
            GetObjectRequest rangeObjectRequest = new GetObjectRequest(bucketName, key)
                                                        .withRange(0,9);
            objectPortion = s3Client.getObject(rangeObjectRequest);
            System.out.println("Printing bytes retrieved.");
            displayTextInputStream(objectPortion.getObjectContent());
            
            // Get an entire object, overriding the specified response headers, and print the object's content.
            ResponseHeaderOverrides headerOverrides = new ResponseHeaderOverrides()
                                                            .withCacheControl("No-cache")
                                                            .withContentDisposition("attachment; filename=example.txt");
            GetObjectRequest getObjectRequestHeaderOverride = new GetObjectRequest(bucketName, key)
                                                            .withResponseHeaders(headerOverrides);
            headerOverrideObject = s3Client.getObject(getObjectRequestHeaderOverride);
            displayTextInputStream(headerOverrideObject.getObjectContent());*/
        }
        catch(AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process 
            // it, so it returned an error response.
            e.printStackTrace();
        }
        catch(SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
        }
        finally {
            // To ensure that the network connection doesn't remain open, close any open input streams.
          /*  if(fullObject != null) {
                fullObject.close();
            }
            if(objectPortion != null) {
                objectPortion.close();
            }
            if(headerOverrideObject != null) {
                headerOverrideObject.close();
            }*/
        }
	}
	
	private static void displayTextInputStream(InputStream input) throws IOException {
        // Read the text input stream one line at a time and display each line.
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line = null;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        System.out.println();
    }
	
	private static BufferedImage getImage(InputStream input) throws IOException {
		BufferedImage myPicture = ImageIO.read(input);
		
		AffineTransform tx = new AffineTransform();
		tx.rotate(0.5, myPicture.getWidth() / 2, myPicture.getHeight() / 2);

		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
		myPicture = op.filter(myPicture, null);
		
		return myPicture;

	}
	
    private static ByteBuffer getRandomByteBuffer(int size) throws IOException {
        byte[] b = new byte[size];
        new Random().nextBytes(b);
        return ByteBuffer.wrap(b);
    }
	
	public void transformTheImage() {
		
	}
}