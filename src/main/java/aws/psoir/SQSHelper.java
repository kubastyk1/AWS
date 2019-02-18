package aws.psoir;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SetQueueAttributesRequest;

public class SQSHelper {

	private AmazonSQS sqs;
	
	public SQSHelper(AmazonSQS sqs) {
		this.sqs = sqs;
	}	
	
	public List<String> receivePictureUrlsFromSQS() {
		List<String> queueList = sqs.listQueues().getQueueUrls();	
		List<String> pictureUrlList = new ArrayList<String>();
		
		String queueUrl = queueList.get(0);
		ReceiveMessageRequest receiveMessageRequest = configureSQSQueue(queueUrl);
		
		System.out.println("Receiving messages from MyQueue.\n");
		final List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
		for (final Message message : messages) {
			printMessageInfo(message);
			
			String pictureUrl = message.getBody();		
			if (pictureUrl != "") {
				pictureUrlList.add(pictureUrl);			
			}

			System.out.println("Deleting a message.\n");
			deleteMessage(message, queueUrl);
		}	
		
		return pictureUrlList;
	}
	
	private ReceiveMessageRequest configureSQSQueue(String queueUrl) {
		final SetQueueAttributesRequest setQueueAttributesRequest = new SetQueueAttributesRequest()
		        .withQueueUrl(queueUrl)
		        .addAttributesEntry("ReceiveMessageWaitTimeSeconds", "20");
		sqs.setQueueAttributes(setQueueAttributesRequest);
		
		return new ReceiveMessageRequest()
				.withQueueUrl(queueUrl)
				.withWaitTimeSeconds(20);
	}
	
	private void deleteMessage(Message message, String queueUrl) {
		final String messageReceiptHandle = message.getReceiptHandle();
		sqs.deleteMessage(new DeleteMessageRequest(queueUrl, messageReceiptHandle));
	}
	
	private void printMessageInfo(Message message) {
		System.out.println("Message");
		System.out.println("  MessageId:     " + message.getMessageId());
		System.out.println("  ReceiptHandle: " + message.getReceiptHandle());
		System.out.println("  MD5OfBody:     " + message.getMD5OfBody());
		System.out.println("  Body:          " + message.getBody());
	}
}
