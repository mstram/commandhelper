/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.events;

import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.CString;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Constructs.IVariable;
import com.laytonsmith.aliasengine.GenericTree;
import com.laytonsmith.aliasengine.GenericTreeNode;
import com.laytonsmith.aliasengine.GenericTreeTraversalOrderEnum;
import com.laytonsmith.aliasengine.Script;
import com.laytonsmith.aliasengine.functions.exceptions.EventException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author layton
 */
public class BoundEvent implements Comparable<BoundEvent> {
    String eventName;
    String id;
    String priority;
    Map<String, Construct> prefilter;
    String eventObjName;    
    Map<String, IVariable> vars;
    List<String> custom_names = new ArrayList<String>();
    GenericTreeNode<Construct> tree;
    org.bukkit.event.Event.Type driver; //For efficiency sake, cache it here
    
    public BoundEvent(String name, CArray options, CArray prefilter, String eventObjName, 
            List<IVariable> vars, GenericTreeNode<Construct> tree) throws EventException{
        this.eventName = name;
        
        if(options != null && options.contains("id")){
            this.id = options.get("id").val();
            if(this.id.matches(".*?:\\d*?")){
                throw new EventException("The id given may not match the format\"string:number\"");
            }
        } else {
            //Generate a new event id
            id = name + ":" + EventHandler.GetUniqueID();
        }
        if(options != null && options.contains("priority")){
            this.priority = options.get("priority").val().toUpperCase();
        } else {
            this.priority = "NORMAL";
        }
        if(!(
                this.priority.equals("LOWEST") ||
                this.priority.equals("LOW") ||
                this.priority.equals("NORMAL") ||
                this.priority.equals("HIGH") ||
                this.priority.equals("HIGHEST") ||
                this.priority.equals("MONITOR")
                )){
            throw new EventException("Priority must be one of: LOWEST, LOW, NORMAL, HIGH, HIGHEST, MONITOR");
        }
        
        this.prefilter = new HashMap<String, Construct>();
        if(prefilter != null){
            for(Construct key : prefilter.keySet()){
                String k = key.val();
                this.prefilter.put(k, prefilter.get(key, 0));
            }
        }
        
        this.vars = new HashMap<String, IVariable>();
        for(IVariable v : vars){
            this.vars.put(v.getName(), v);
        }
        this.tree = tree;      
        
        this.driver = EventList.getEvent(this.eventName).driver();
        this.eventObjName = eventObjName;
        
        for(IVariable v : vars){
            custom_names.add(v.getName());
        }
    }
    
    public String getEventName(){
        return eventName;
    }

    public String getEventObjName() {
        return eventObjName;
    }
    
    public org.bukkit.event.Event.Type getDriver(){
        return driver;
    }

    public String getId() {
        return id;
    }

    public Map<String, Construct> getPrefilter() {
        return prefilter;
    }

    public Priority getPriority() {
        return Priority.valueOf(priority);
    }
    public enum Priority{
        LOWEST(1),
        LOW(2),
        NORMAL(3),
        HIGH(4),
        HIGHEST(5),
        MONITOR(6);
        private final int id;
        private Priority(int i){
            this.id = i;
        }
        public int getId(){
            return this.id;
        }
    }
    public int compareTo(BoundEvent o) {
       if(this.getPriority().getId() < o.getPriority().getId()){
           return -1;
       } else if(this.getPriority().getId() > o.getPriority().getId()){
           return 1;
       } else {
           return 0;
       }
    }
    
    public void trigger(Map<String, Construct> event){
        GenericTree<Construct> root = new GenericTree<Construct>();
        root.setRoot(tree);
        for(GenericTreeNode<Construct> node : root.build(GenericTreeTraversalOrderEnum.PRE_ORDER)){
            Construct c = node.getData();
            if(c instanceof IVariable){
                IVariable var = ((IVariable)c);
                if(custom_names.contains(var.getName())){
                    try {
                        //Custom variable
                        var.setIval(this.vars.get(var.getName()).ival().clone());
                    } catch (CloneNotSupportedException ex) {
                        Logger.getLogger(BoundEvent.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else if(var.getName().equals(eventObjName)){
                    //Event object
                    CArray ca = new CArray(0, null);
                    for(String key : event.keySet()){
                        ca.set(new CString(key, 0, null), event.get(key));
                    }
                    var.setIval(ca);
                } else {
                    //Set the default value
                    var.setIval(new CString("", 0, null));
                }
            }
        }
        Script s = Script.GenerateScript(tree);
        s.run(null, null, null);
    }
}
