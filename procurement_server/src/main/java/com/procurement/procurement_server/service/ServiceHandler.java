package com.procurement.procurement_server.service;

import com.procurement.procurement_server.model.order_level.Order;
import com.procurement.procurement_server.model.order_level.Requistion;
import com.procurement.procurement_server.model.supplier_level.Item;
import com.procurement.procurement_server.model.user_level.User;
import com.procurement.procurement_server.service.Item_service.ItemService;
import com.procurement.procurement_server.service.order_service.*;
import com.procurement.procurement_server.service.order_service.builder.ApprovedOrder;
import com.procurement.procurement_server.service.order_service.builder.OrderBroker;
import com.procurement.procurement_server.service.order_service.builder.OrderBuilder;
import com.procurement.procurement_server.service.order_service.builder.PendingOrder;
import com.procurement.procurement_server.service.user_service.UserService;
import com.procurement.procurement_server.util.*;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class ServiceHandler {

	private static boolean isInitialized = false;

	@Autowired
	UserService userService;
	
	@Autowired
	ItemService itemService;

	@Autowired
	DataServer dataServer;

	@Autowired
	OrderServiceImpl orderService;

	public ResponseEntity handleServiceRequest(String reqId, Object obj, String uid) {
		if (!isIsInitialized()) {
			startDataServer();
			setIsInitialized(true);
		}
		
		System.out.println("##########service handler  with Item");
		switch (Integer.parseInt(reqId)) {
		case CommonConstants.GET_USER_REQUEST:
			return getRequiredUser(obj);
		case CommonConstants.ADD_USER_REQUEST:
			return addNewUser(obj);
/*-----Item ------*/
		case CommonConstants.ADD_ITEM_REQUEST:
			return addNewItem((Item) obj);
//---Item
		case CommonConstants.GET_ALL_USERS:
			return getAllUsers();
		case CommonConstants.DELETE_SPECIFIC_USER:
			return deleteSpecificUser(uid);
		case CommonConstants.ADD_ORDER_REQUEST:
			return handleOrder((Order) obj);
		case CommonConstants.UPDATE_ORDER_REQUEST:
			return handleOrder((Order) obj);
//                case CommonConstants.GET_ALL_ORDERS:
//                    return getAllOrders();
		default:
			return new ResponseEntity("Failed", HttpStatus.OK);
		}
	}

	private ResponseEntity addNewItem(Item obj) {
		return itemService.addNewItem(obj);
		
	}

	private ResponseEntity getRequiredUser(Object obj) {
		return userService.getRequiredUser((User) obj);
	}

	private ResponseEntity addNewUser(Object obj) {
		return userService.addNewUser(obj);
	}

	private void startDataServer() {
		dataServer.startDataServer();
	}

	public static boolean isIsInitialized() {
		return isInitialized;
	}

	public static void setIsInitialized(boolean isInitialized) {
		ServiceHandler.isInitialized = isInitialized;
	}

	public ResponseEntity<Object> handleOrder(Order order) {

		int orderItemQuantity = orderService.calculateQuantity(order.getItems());
		double orderTotal = orderService.calculateTotal(order.getItems());

		Requistion requisition = new Requistion();

		ApprovedOrder approveOrder = new ApprovedOrder(requisition);
		PendingOrder pendingOrder = new PendingOrder(requisition);

		if (order.get_idAsObjectId() == null) {
			System.out.println("id is null");
			requisition.set_id(new ObjectId());
		} else {
			System.out.println("id is not null");
			requisition = order.getRequistion();
		}

		/*-----------------------------------------------------------*/

		OrderBroker broker = new OrderBroker();

		if (orderTotal > CommonConstants.ORDER_LIMIT) {
			broker.takeOrder(pendingOrder);
			requisition = broker.placeOrder();
		} else {
			broker.takeOrder(approveOrder);
			requisition = broker.placeOrder();
		}

		Order newOrder = new OrderBuilder(order.get_idAsObjectId()).setItems(order.getItems())
				.setOrderDate(Generator.getCurrentDate()).setPayment(null).setQuantity(orderItemQuantity)
				.setRequisition(null).setTotalAmount(orderTotal).build();

		return new ResponseEntity<>(orderService.addOrder(newOrder, requisition), HttpStatus.OK);

	}

	private ResponseEntity getAllUsers() {
		return userService.getAllUsers();
	}

	private ResponseEntity deleteSpecificUser(String uid) {
		return userService.deleteSpecificUser(uid);
	}

}
