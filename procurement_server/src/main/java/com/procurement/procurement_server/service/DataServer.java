package com.procurement.procurement_server.service;


import com.procurement.procurement_server.dao.ItemRepo;
import com.procurement.procurement_server.datastore.ItemDataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.procurement.procurement_server.dao.user_dao.UserRepo;
import com.procurement.procurement_server.datastore.UserDatastore;
import com.procurement.procurement_server.model.user_level.User;

/**
 *This class will load all the db data and create cache tables
 *
 */
@Service
public class DataServer {


    @Autowired
    UserRepo userRepo;

    @Autowired
    ItemRepo itemRepo;

    public User getUser(String key) {
        return UserDatastore.getSharedInstance().getUserFromStore(key);
    }

    public void startDataServer() {
        try {
            UserDatastore.getSharedInstance().setDataStore(userRepo.findAll());
            ItemDataStore.getSharedInstance().setDataStore(itemRepo.findAll());
        }catch (Exception e) {
            UserDatastore.getSharedInstance().setDataStore(userRepo.findAll());
            ItemDataStore.getSharedInstance().setDataStore(itemRepo.findAll());
        }
    }
}
