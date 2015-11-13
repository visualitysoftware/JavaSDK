package io.cloudboost;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

public class QueueMessage {
	private ACL acl;
	private long timeout;
	private String message;
	private JSONObject document;
	private ArrayList<String> _modifiedColumns;
	private String type="queue-message";
	public QueueMessage() {
		this._modifiedColumns = new ArrayList<String>();
		this._modifiedColumns.add("createdAt");
		this._modifiedColumns.add("updatedAt");
		this._modifiedColumns.add("ACL");
		this._modifiedColumns.add("expires");
		this.acl = new ACL();
		document = new JSONObject();
		try {
			document.put("_id", (Object) null);
			document.put("timeout", timeout);
			document.put("delay", (Object) null);

			document.put("_type",type);
			document.put("ACL", acl.getACL());
			document.put("expires", JSONObject.NULL);
			document.put("_modifiedColumns", this._modifiedColumns);
			document.put("_isModified", true);
			document.put("message", message);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public boolean hasKey(String key){
		return document.has(key);
	}
	public String getId(){
		return (String) getElement("_id");
	}
	public void setDelay(Object delay){
		addElement("delay", delay);
	}
	public void push(String message){
		this.message=message;
		addElement("message", message);
	
	}
	public ACL getAcl() {
		return acl;
	}
	public void setAcl(ACL acl) {
		this.acl = acl;
	}
	public long getTimeout() {
		return timeout;
	}
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
	public String getMessage() {
		return (String) getElement("message");
		
	}
	public void addElement(String key,Object val){
		try {
			document.put(key, val);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private Object getElement(String key){
		Object obj=null;
		try {
			obj=document.get(key);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return obj;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public JSONObject getDocument() {
		return document;
	}
	public void setDocument(JSONObject document) {
		this.document = document;
	}
	public ArrayList<String> get_modifiedColumns() {
		return _modifiedColumns;
	}
	public void set_modifiedColumns(ArrayList<String> _modifiedColumns) {
		this._modifiedColumns = _modifiedColumns;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
}
