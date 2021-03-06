package com.procurement.procurement_server.service;


import com.procurement.procurement_server.model.order_level.Order;
import com.procurement.procurement_server.model.order_level.Requistion;
import com.procurement.procurement_server.model.site_level.Site;
import com.procurement.procurement_server.model.supplier_level.Item;
import com.procurement.procurement_server.model.user_level.User;
import com.procurement.procurement_server.service.Item_service.ItemService;
import com.procurement.procurement_server.service.order_service.*;
import com.procurement.procurement_server.service.order_service.builder.ApprovedOrder;
import com.procurement.procurement_server.service.order_service.builder.OrderBroker;
import com.procurement.procurement_server.service.order_service.builder.OrderBuilder;
import com.procurement.procurement_server.service.order_service.builder.PendingOrder;
import com.procurement.procurement_server.service.site_service.SiteServiceImpl;
import com.procurement.procurement_server.service.user_service.StaffService;
import com.procurement.procurement_server.service.user_service.UserService;
import com.procurement.procurement_server.util.*;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ServiceHandler {

    private static boolean isInitialized = false;

	@Autowired
	UserService userService;

    @Autowired
    DataServer dataServer;

    @Autowired
    OrderServiceImpl orderService;

    @Autowired
    SiteServiceImpl siteService;

    @Autowired
    StaffService staffService;

  	@Autowired
    ItemService itemService;
    public ResponseEntity handleServiceRequest(String reqId, Object obj, String uid) {
        if (!isIsInitialized()) {
            startDataServer();
            setIsInitialized(true);
        }
        switch (Integer.parseInt(reqId)) {
            case CommonConstants.GET_USER_REQUEST:
                return getRequiredUser(obj);
            case CommonConstants.ADD_USER_REQUEST:
                return addNewUser(obj);
            case CommonConstants.GET_ALL_USERS:
                return getAllUsers();
            case CommonConstants.DELETE_SPECIFIC_USER:
                return deleteSpecificUser(uid);
            case CommonConstants.ADD_ORDER_REQUEST:
            	return handleOrder((Order)obj);
            case CommonConstants.UPDATE_ORDER_REQUEST :
            	return handleOrder((Order) obj);
            case CommonConstants.GET_ALL_ORDERS:
                    return getAllOrders();
            case CommonConstants.ADD_SITE_REQUEST:
                return addSite((Site) obj);
            case CommonConstants.GET_ALL_SITE_REQUEST:
                return getAllSites();
            case CommonConstants.GET_STAFF_BY_TYPE :
            	return getStaffMembersByType(uid);
            case CommonConstants.GET_AVAILABLE_SUPPLIER_ITEMS:
               return getItemWithQty();
            case CommonConstants.ADD_ITEM_REQUEST:
               return addNewItem((Item) obj);
            case CommonConstants.GET_ITEM_BY_QTY:
               return getItemWithQty();
            case CommonConstants.GET_ITEM_BY_NON_QTY:
               return getItemWithoutQty();
            default:
                return new ResponseEntity("Failed", HttpStatus.OK);
        }
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

    public ResponseEntity<Object> handleOrder(Order order ) {


    		int orderItemQuantity = orderService.calculateQuantity(order.getItems());
    		double orderTotal = orderService.calculateTotal(order.getItems());

    		Requistion requisition = new Requistion();


    		ApprovedOrder approveOrder = new ApprovedOrder(requisition);
    		PendingOrder pendingOrder = new PendingOrder(requisition);


    		if( order.get_idAsObjectId() == null ) {
    			System.out.println("id is null");
    			requisition.set_id(new ObjectId());
    		}else {
    			System.out.println("id is not null");
    			requisition = order.getRequistion();
    		}

    		/*-----------------------------------------------------------*/

    		OrderBroker broker = new OrderBroker();

    		if( orderTotal > CommonConstants.ORDER_LIMIT ) {
    			broker.takeOrder(pendingOrder);
    			requisition =  broker.placeOrder();
    		}else {
    			broker.takeOrder(approveOrder);
    			requisition = broker.placeOrder();
    		}


    		Order newOrder = new OrderBuilder(order.get_idAsObjectId())
    				.setItems(order.getItems())
    				.setOrderDate(Generator.getCurrentDate())
    				.setPayment(null)
    				.setQuantity(orderItemQuantity)
    				.setRequisition(null)
    				.setTotalAmount(orderTotal)
    				.setOrderPlacedUser(order.getPlacedUser())
    				.setOrderApprovedUser(null)
    				.build();

    		return new ResponseEntity<>(orderService.addOrder(newOrder, requisition), HttpStatus.OK);


    }

    private ResponseEntity addNewItem(Item obj) {
		return itemService.addNewItem(obj);

	}



	public ResponseEntity<Object> getAllOrders(){
		
		return new ResponseEntity<>(orderService.getAllOrders(), HttpStatus.OK);
		
	}

	public ResponseEntity<Object> getOrdersByStatus( String status ) {
		System.out.println("Status : " +  status);
		return new ResponseEntity<>(orderService.getOrdersByStatus(status), HttpStatus.OK);
	}
	
	public ResponseEntity<Object> approveOrder( Order order ){
	
		return new ResponseEntity<>(orderService.approveOrder(order) , HttpStatus.OK);
		
	}
	
	public ResponseEntity<Object> declineOrder( Order order ){
		return new ResponseEntity<>(orderService.declineOrder(order) , HttpStatus.OK);
	}

	private ResponseEntity deleteSpecificUser(String uid) {
		return userService.deleteSpecificUser(uid);
	}

    private ResponseEntity getAllUsers() {
        return userService.getAllUsers();
    }



    private ResponseEntity addSite(Site site) {
    	return siteService.addSite(site);
    }

    private ResponseEntity getAllSites() {
    	return siteService.getAllSites();
    }


    private  ResponseEntity getItemWithoutQty() {
      return itemService.getItemWithoutQty();
    }

    private  ResponseEntity getItemWithQty() {
      return itemService.getItemWithQty();

    }

    private ResponseEntity getStaffMembersByType(String type) {
    	return staffService.getStaffMembersByType(type);
    }
}
