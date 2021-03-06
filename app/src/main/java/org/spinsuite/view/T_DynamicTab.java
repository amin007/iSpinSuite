/*************************************************************************************
 * Product: Spin-Suite (Making your Business Spin)                                   *
 * This program is free software; you can redistribute it and/or modify it           *
 * under the terms version 2 of the GNU General Public License as published          *
 * by the Free Software Foundation. This program is distributed in the hope          *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied        *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                  *
 * See the GNU General Public License for more details.                              *
 * You should have received a copy of the GNU General Public License along           *
 * with this program; if not, write to the Free Software Foundation, Inc.,           *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                            *
 * For the text or an alternative of this public license, you may reach us           *
 * Copyright (C) 2012-2014 E.R.P. Consultores y Asociados, S.A. All Rights Reserved. *
 * Contributor(s): Yamel Senih www.erpcya.com                                        *
 *************************************************************************************/
package org.spinsuite.view;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;

import org.spinsuite.base.DB;
import org.spinsuite.base.R;
import org.spinsuite.bchat.view.V_BChat;
import org.spinsuite.interfaces.I_DT_FragmentSelectListener;
import org.spinsuite.interfaces.OnFieldChangeListener;
import org.spinsuite.process.DocAction;
import org.spinsuite.util.AttachmentHandler;
import org.spinsuite.util.DisplayMenuItem;
import org.spinsuite.util.DisplayRecordItem;
import org.spinsuite.util.DisplayType;
import org.spinsuite.util.Env;
import org.spinsuite.util.FilterValue;
import org.spinsuite.util.LogM;
import org.spinsuite.util.Msg;
import org.spinsuite.util.TabParameter;
import org.spinsuite.view.lookup.GridField;
import org.spinsuite.view.lookup.GridTab;
import org.spinsuite.view.lookup.InfoField;
import org.spinsuite.view.lookup.InfoTab;
import org.spinsuite.view.lookup.Lookup;
import org.spinsuite.view.lookup.VLookupButton;
import org.spinsuite.view.lookup.VLookupButtonDocAction;
import org.spinsuite.view.lookup.VLookupButtonPaymentRule;
import org.spinsuite.view.lookup.VLookupCheckBox;
import org.spinsuite.view.lookup.VLookupDateBox;
import org.spinsuite.view.lookup.VLookupNumber;
import org.spinsuite.view.lookup.VLookupSearch;
import org.spinsuite.view.lookup.VLookupSpinner;
import org.spinsuite.view.lookup.VLookupString;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TableLayout;

/**
 * @author Yamel Senih
 *
 */
public class T_DynamicTab extends T_FormTab 
						implements I_DT_FragmentSelectListener {
	
	/**
	 * 
	 * *** Constructor ***
	 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com 01/04/2014, 22:32:14
	 */
	public T_DynamicTab() {
		//	
	}
	
	/**
	 * Set Tab From
	 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com 02/04/2014, 22:09:38
	 * @param m_FromTab
	 * @return void
	 */
	public void setFromTab(T_FormTab m_FromTab) {
		this.m_FromTab = m_FromTab; 
	}

	/**	Parameters	*/
	private 	DB 						conn 				= null;
	private 	GridTab 				mGridTab			= null;
	private 	InfoTab 				tabInfo				= null;
	private 	ScrollView 				v_scroll			= null;
	private 	TableLayout 			v_tableLayout		= null;
	private 	boolean					m_IsLoadDataOk		= false;
	/**	From Tab					*/
	private 	T_FormTab				m_FromTab			= null;
	/**	Listener					*/
	private 	OnFieldChangeListener	m_Listener			= null;
	/**	View 						*/
	private 	View 					m_view 				= null;
	/**	Attachment Handler			*/
	private 	AttachmentHandler 		m_AttHandler		= null;
	
	/**	Current Status				*/
	protected static final int 			NEW 				= 0;
	protected static final int 			MODIFY 				= 1;
	protected static final int 			SEE 				= 3;
	protected static final int 			DELETED 			= 4;
	
	/**	Option Menu Item			*/
	private final int O_BUSINESS_CHAT						= 1;
	private final int O_SHARE								= 2;
	private final int O_DELETE								= 3;
	private final int O_ATTACH_PHOTO						= 4;
	private final int O_ATTACH_FILE							= 5;
	private final int O_VIEW_ATTACH							= 6;
	private final int O_VIEW_PREFERENCES					= 7;
	private final int O_SYNCHRONIZE							= 8;
	private final int O_VIEW_SYNCHRONIZATION				= 9;
	
	/**	Option Menu					*/
	private MenuItem mi_Search 								= null;
	private MenuItem mi_Edit 								= null;
	private MenuItem mi_Add 								= null;
	private MenuItem mi_More 								= null;
	private MenuItem mi_Cancel 								= null;
	private MenuItem mi_Save 								= null;
	
	/**	Results						*/
	private static final int 		ACTION_TAKE_FILE		= 3;
	private static final int 		ACTION_TAKE_PHOTO		= 4;
	
	/**	Constants Type Save			*/
	private static final String 	RECORD_SAVE				= "RS";
	private static final String 	PHOTO_ATTACHMENT_SAVE	= "PS";
	private static final String 	FILE_ATTACHMENT_SAVE	= "FS";
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {		
		if(m_view != null)
			return m_view;
		//	Inflate
		m_view =  inflater.inflate(R.layout.t_dynamic_tab, container, false);
    	//	Scroll
    	v_scroll = (ScrollView) m_view.findViewById(R.id.sv_DynamicTab);
		return m_view;
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	//	Set Is Read Write
    	//	Instance Listener
    	m_Listener = new OnFieldChangeListener() {
    		@Override
    		public void onFieldEvent(GridField mField) {
    			LogM.log(getActivity(), T_DynamicTab.class, 
    					Level.FINE, "Field Event = " + mField.getColumnName());
    			//	
    			if(isModifying()) {
        			//	Process Callout
        			processCallout(mField);
        			//	Reload depending fields
        			changeDepending(mField);
        			
    			} else if(mField.getColumnName().equals("DocAction")) {
    				//	Valid Ok
    				VLookupButtonDocAction docAction = (VLookupButtonDocAction) mField;
    				if(docAction.isProcessed()) {
    					//	Save Model
    					boolean ok = mGridTab.modelSave();
    					if(ok)
    						refreshFromChange(true);
    					else 
    						Msg.alertMsg(getActivity(), mGridTab.getError());
    				} else {
    					Msg.alertMsg(getActivity(), docAction.getProcessMsg());
    				}
    				//	
    				return;
    			}
    			//	Change Display Dependent
    			if(m_IsLoadDataOk)
    				mGridTab.changeDisplayDepending(mField);
    		}
		};
    	//	Init Load
		if(!isLoadOk())
			initLoad();
	}
	
	/**
	 * Process Callout
	 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com 20/05/2014, 10:27:16
	 * @param mField
	 * @return void
	 */
	private void processCallout(GridField mField) {
		//	No Action
		if(mField.getColumnName().equals("DocAction"))
			return;
		//	Process Document No
		//if(mField.getColumnName().equals("C_Doctype_ID") 
			//	|| mField.getColumnName().equals("C_DocTypeTarget_ID")) {
			//processDocumentNo(mField);
			//return;
		//}
		//	Verify if is changed
		if(!mField.isChanged())
			return;
		//	Log
		LogM.log(getActivity(), T_DynamicTab.class, 
				Level.FINE, "processCallout(" + mField.getColumnName() + ")");
		//	
		String retValue = mGridTab.processCallout(mField);
		//	Show Error
		if(retValue != null
				&& retValue.length() != 0)
			Msg.toastMsg(getActivity(), getString(R.string.msg_Error) + ": " + retValue);
	}
	
	/**
	 * Process Document No
	 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com 23/10/2014, 10:22:37
	 * @param mField
	 * @return void
	 */
	/*private void processDocumentNo(GridField mField) {
		//	Verify Value
		int m_C_DocType_ID = mField.getValueAsInt();
		if(m_C_DocType_ID <= 0)
			return;
		//	Verify if Exists Column
		GridField fieldDocument = mGridTab.getField("DocumentNo");
		if(fieldDocument == null)
			return;
		//	Get New Document No
		String seqNo = MSequence.getDocumentNo(getActivity(), 
				m_C_DocType_ID, tabInfo.getTableName(), true, null);
		//	
		mGridTab.setValue("DocumentNo", seqNo);
	}*/
	
	/**
	 * Reload depending field
	 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com 30/08/2014, 20:25:24
	 * @param mField
	 * @return void
	 */
	private void changeDepending(GridField mField) {
		//	Log
		LogM.log(getActivity(), T_DynamicTab.class, 
				Level.FINE, "processCallout(" + mField.getColumnName() + ")");
		if(!mField.getColumnName().equals("DocAction")) {
			mGridTab.changeDepending(mField);
		}
	}
	
	/**
	 * Init Fragment
	 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com 02/04/2014, 13:50:51
	 * @return void
	 */
	private void initLoad() {
		//	
		setIsLoadOk(true);
    	//	Retain Instance
    	if(getTabLevel() == 0)
    		setRetainInstance(true);    	
    	//	Table Layout
    	v_tableLayout = new TableLayout(getActivity());
    	//	Add View
    	v_scroll.addView(v_tableLayout);
    	//	
    	new LoadViewTask().execute(v_tableLayout);
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setHasOptionsMenu(true);
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
	    //if(!Env.isCurrentTab(getActivity(), 
	    	//	tabParam.getActivityNo(), tabParam.getTabNo()))
	    	//return;
        //	
        menu.clear();
        inflater.inflate(R.menu.dynamic_tab, menu);
    	//	do it
        //	Get Items
        mi_Search 	= menu.getItem(0);
        mi_Edit 	= menu.getItem(1);
        mi_Add	 	= menu.getItem(2);
        mi_More 	= menu.getItem(3);
        mi_Cancel 	= menu.getItem(4);
        mi_Save 	= menu.getItem(5);
    	//	Valid is Loaded
    	if(!isLoadOk())
    		return;
        //	Lock View
    	changeMenuView();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	int itemId = item.getItemId();
		if (itemId == R.id.action_search) {
			Bundle bundle = new Bundle();
			bundle.putInt("SPS_Table_ID", tabInfo.getSPS_Table_ID());
			bundle.putInt("SPS_Tab_ID", tabInfo.getSPS_Tab_ID());
			bundle.putString("Name", getName());
			bundle.putString("IsInsertRecord", (!isReadOnly() && isInsertRecord()? "Y": "N"));
			if(getTabLevel() > 0) {
				FilterValue criteria = tabInfo.getCriteria(getActivity(), 
						getActivityNo(), getParentTabNo());
				bundle.putParcelable("Criteria", criteria);
			}
			Intent intent = new Intent(getActivity(), V_StandardSearch.class);
			intent.putExtras(bundle);
			//	Start with result
			startActivityForResult(intent, 0);
			return true;
		} else if (itemId == R.id.action_edit) {
			//	Verify Parent Changed
			if(isParentModifying()) {
    			Msg.toastMsg(getActivity(), "@ParentRecordModified@");
    			return false;
    		}
			//	Do It
			lockView(MODIFY);
			return true;
		} else if (itemId == R.id.action_add) {
			//	Verify Parent Changed
			if(isParentModifying()) {
    			Msg.toastMsg(getActivity(), "@ParentRecordModified@");
    			return false;
    		}
			//	Do It
			newOption();
			return true;
		} else if (itemId == R.id.action_more) {
			//	More
			showPopupMenu();
			return true;
		} else if (itemId == R.id.action_cancel) {
			if(mGridTab.getRecord_ID() <= 0) {
				mGridTab.backCopy();
			}
			refresh(mGridTab.getKeys(), mGridTab.getKeyColumns(), false);
			lockView(SEE);
			return true;
		} else if (itemId == R.id.action_save) {
			//	Verify Parent Changed
			if(isParentModifying()) {
    			Msg.toastMsg(getActivity(), "@ParentRecordModified@");
    			return false;
    		}
			//	Do It
			//	Save Thread
			new SaveTask().execute(RECORD_SAVE);
			return true;
		}
		//	
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Show Popup Menu
     * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com 07/05/2014, 12:08:11
     * @return void
     */
    private void showPopupMenu() {
    	View menuItemView = getActivity().findViewById(R.id.action_more);
		PopupMenu popupMenu = new PopupMenu(getActivity(), menuItemView);
		//	Share Record
		popupMenu.getMenu().add(Menu.NONE, O_BUSINESS_CHAT, 
					Menu.NONE, getString(R.string.Action_BChat));
		//	Share Record
		popupMenu.getMenu().add(Menu.NONE, O_SHARE, 
					Menu.NONE, getString(R.string.Action_Share));
		//	Delete Record
		popupMenu.getMenu().add(Menu.NONE, O_DELETE, 
				Menu.NONE, getString(R.string.Action_Delete));
		//	Attach a Photo
		popupMenu.getMenu().add(Menu.NONE, O_ATTACH_PHOTO, 
				Menu.NONE, getString(R.string.Action_AttachPhoto));
		//	Attach a File
		popupMenu.getMenu().add(Menu.NONE, O_ATTACH_FILE, 
				Menu.NONE, getString(R.string.Action_AttachFile));
		//	View Attachment
		if(mGridTab.getPO() != null
				&& mGridTab.getRecord_ID() > 0) {
	    	//	Instance Attachment
	    	if(m_AttHandler == null)
	    		m_AttHandler = new AttachmentHandler(getActivity(), mGridTab.getSPS_Table_ID());
	    	//	
	    	m_AttHandler.setRecord_ID(mGridTab.getRecord_ID()); 
	    	//	Exist a Attachment
			if(m_AttHandler.hasAttachment())
				popupMenu.getMenu().add(Menu.NONE, O_VIEW_ATTACH, 
						Menu.NONE, getString(R.string.Action_ViewAttachment));
		}
		//	Go to Setup
		popupMenu.getMenu().add(Menu.NONE, O_VIEW_PREFERENCES, 
				Menu.NONE, getString(R.string.Action_Config));
		//	Go to Synchronization Menu
		popupMenu.getMenu().add(Menu.NONE, O_VIEW_SYNCHRONIZATION, 
				Menu.NONE, getString(R.string.Action_Syncronization));
		//	Action
		popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
					case O_BUSINESS_CHAT:
						Intent bChat = new Intent(getActivity(), V_BChat.class);
						startActivity(bChat);
						return true;
					case O_SHARE:
	        			return true;
	        		case O_DELETE:
	        			//	Verify Parent Changed
	        			if(isParentModifying()) {
	            			Msg.toastMsg(getActivity(), "@ParentRecordModified@");
	            			return false;
	            		}
	        			//	Do It
	        			//	Delete
	        			deleteRecord();
	        			return true;
	        		case O_ATTACH_PHOTO:
	        			//	Verify Parent Changed
	        			if(isParentModifying()) {
	            			Msg.toastMsg(getActivity(), "@ParentRecordModified@");
	            			return false;
	            		}
	        			//	Do It
	        			attachPhoto();
	        			return true;
	        		case O_ATTACH_FILE:
	        			//	Verify Parent Changed
	        			if(isParentModifying()) {
	            			Msg.toastMsg(getActivity(), "@ParentRecordModified@");
	            			return false;
	            		}
	        			//	Do It
	        			attachFile();
	        			return true;
	        		case O_VIEW_ATTACH:
	        			viewAttachment();
	        			return true;
	        		case O_VIEW_PREFERENCES:
	        			Intent preferences = new Intent(getActivity(), V_Preferences.class);
	    				startActivity(preferences);
	        			return true;
	        		case O_VIEW_SYNCHRONIZATION:
	        			Intent syncMenu = new Intent(getCallback(), V_Synchronization.class);
	    				startActivity(syncMenu);
	        			return true;
				}
				return false;
			}
		});
		//	Show
		popupMenu.show();
	}
    
    /**
     * View Attachment
     * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com 08/05/2014, 11:00:17
     * @return void
     */
    private void viewAttachment() {
    	//	
    	if(m_AttHandler == null)
    		return;
    	//	
    	Intent intent = new Intent(getActivity(), V_AttachView.class);
    	intent.putExtra("FilePath", m_AttHandler.getAttDirectoryRecord());
    	startActivity(intent);
    }    
    
    /**
     * Delete Record
     * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com 07/05/2014, 14:04:50
     * @return void
     */
    private void deleteRecord() {
		//	Verify Processed	
    	if(mGridTab.isProcessed()
    			|| isProcessed()) {				//	Valid Processed
    		Msg.alertMsg(getActivity(), "@CannotDeleteTrx@");
    		return;
    	} else if(!mGridTab.isDeleteable()) {	//	Valid Deleteable
    		Msg.alertMsg(getActivity(), "@CannotDelete@");
    		return;
    	}
    	//	
    	String msg_Acept = this.getResources().getString(R.string.msg_Acept);
		Builder ask = Msg.confirmMsg(getActivity(), "@DeleteRecord?@");
		ask.setPositiveButton(msg_Acept, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				if(mGridTab.delete()) {
					refresh(0, false);
					lockView(DELETED);
		    		//	Refresh
		    		refreshIndex();
				} else {
					Msg.alertMsg(getActivity(), mGridTab.getError());
				}
			}
		});
		ask.show();
    }
    
    /**
     * Action Attach a Photo
     * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com 07/05/2014, 15:29:45
     * @return void
     */
    private void attachPhoto() {
    	//	Instance Attachment
    	if(m_AttHandler == null)
    		m_AttHandler = new AttachmentHandler(getActivity(), mGridTab.getSPS_Table_ID());
    	//	
    	m_AttHandler.setRecord_ID(mGridTab.getRecord_ID());
    	//	Delete Temp File
    	File tmpFile = new File(m_AttHandler.getTMPImageName());
    	if(tmpFile.exists()) {
    		if(!tmpFile.delete())
    			tmpFile.deleteOnExit();
    	}
    	//	
    	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    	intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tmpFile));
	    getActivity().startActivityForResult(intent, ACTION_TAKE_PHOTO);
	}

    /**
     * Action Attach a File
     * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com 07/05/2014, 15:29:45
     * @return void
     */
    private void attachFile() {
    	Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		//	Set Data Type
		intent.setType("*/*");
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		//	Start Activity
		getCallback().startActivityForResult(intent, ACTION_TAKE_FILE);
    }
    
    /**
     * Refresh Header Index
     * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com 04/04/2014, 08:17:32
     * @return void
     */
    private void refreshIndex() {
    	//	Refresh
    	if(m_FromTab != null)
    		m_FromTab.refreshFromChange(true);
    	//	Refresh
    	if(getCallback() != null) {
    		getCallback().requestRefreshAll(true);
    	}
    }
    
    /**
     * Handle lock view
     * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com 06/03/2014, 16:14:12
     * @param mode
     * @return void
     */
    private void lockView(int mode) {
    	//	If New Record
    	if(mode == NEW
    			|| mode == MODIFY) {
    		mi_Cancel.setVisible(true);
    		mi_Save.setVisible(!isReadOnly() 
    				&& ((isInsertRecord() && mode == NEW) 
    						|| mode == MODIFY));
    		mi_More.setVisible(false);
    		mi_Add.setVisible(false);
    		mi_Edit.setVisible(false);
    		mi_Search.setVisible(false);
    		setIsModifying(true);
    	} else if(mode == DELETED) {
    		mi_Cancel.setVisible(false);
    		mi_Save.setVisible(false);
    		mi_More.setVisible(false);
    		mi_Add.setVisible(!isReadOnly() && isInsertRecord());
    		mi_Edit.setVisible(false);
    		mi_Search.setVisible(true);
    		setIsModifying(false);
    	} else if(mode == SEE) {
    		mi_Cancel.setVisible(false);
    		mi_Save.setVisible(false);
    		mi_More.setVisible(mGridTab!= null 
    				&& mGridTab.getRecord_ID() > 0);
    		//	Verify Processed
    		boolean m_IsProcessed = isProcessed();
    		//	
//    		if(m_TabParam.getTabLevel() > 0
//    				&& getActivity() != null)
//    			m_IsProcessed = Env.getContextAsBoolean(
//    				m_TabParam.getActivityNo(), m_TabParam.getParentTabNo(), "Processed");
    		//	
    		mi_Add.setVisible(!isReadOnly() 
    				&& isInsertRecord() 
    				&& !m_IsProcessed);
    		mi_Edit.setVisible(mGridTab != null 
    				&& mGridTab.getRecord_ID() > 0
    				&& !isReadOnly());
    		mi_Search.setVisible(true);
    		setIsModifying(false);
    	}
		//	Enabled
    	//	
    	enableView(mode);
    }
    
    /**
     * Enable Elements
     * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com 06/03/2014, 17:38:53
     * @param mode
     * @return void
     */
    public void enableView(int mode) {
		if(mGridTab == null)
			return;
    	if(mode == NEW) {
			mGridTab.dataNew();
		} else if(mode == MODIFY) {
			mGridTab.dataModify();
		} else if(mode == DELETED) {
			mGridTab.dataDeleted();
		} else if(mode == SEE) {
			mGridTab.dataSee(!isReadOnly());
		}
	}
    
    /**
     * New Option
     * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com 08/03/2014, 00:22:33
     * @return void
     */
    private void newOption() {
    	//	Backup
    	if(mGridTab != null 
    			&& mGridTab.getRecord_ID() > 0) {
    		mGridTab.copyValues(true);
    	}
    	//	
    	refresh(-1, false);
    	//	
    	lockView(NEW);
    }
    
    /**
     * Refresh Grid Tab
     * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com 19/05/2014, 21:48:52
     * @param record_ID
     * @param parentChanged
     * @return boolean
     */
    private boolean refresh(int record_ID, boolean parentChanged) {
    	return refresh(new int[]{record_ID}, null, parentChanged);
    }
    
    /**
     * Refresh
     * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com 18/10/2014, 13:57:52
     * @param record_ID
     * @param keyColumn
     * @param parentChanged
     * @return
     * @return boolean
     */
    private boolean refresh(int[] record_ID, String [] keyColumn, boolean parentChanged) {
    	//	Refresh Child Index
    	if(mGridTab.getRecord_ID() != record_ID[0])
    		refreshIndex();
    	boolean ok = mGridTab.refresh(record_ID, keyColumn, parentChanged);
    	//	
    	if(ok)
    		changeMenuView();
    	//	
    	return ok;
    }
    
    /**
     * Handle menu items
     * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com 12/03/2014, 23:57:35
     * @return void
     */
    @Override
    public void handleMenu() {
    	//	Valid is Loaded
    	if(!isLoadOk())
    		return;
    	//	do it
    	changeMenuView();
    }
    
    /**
     * Change Menu View
     * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com 02/04/2014, 18:17:58
     * @return void
     */
    private void changeMenuView() {
    	if(mi_Search != null) {
            //	Lock View
    		if(isModifying()) {
    			if(mGridTab != null
    					&& mGridTab.getRecord_ID() <= 0)
    				lockView(NEW);
    			else
    				lockView(MODIFY);
    		} else
        		lockView(SEE);
        }
    }
    
    /**
     * Refresh from parent activity
     * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com 16/03/2014, 10:53:18
     * @return boolean
     */
    public boolean refreshFromChange(boolean reQuery) {
    	//	Valid is Loaded
    	boolean ok = false;
    	if(!isLoadOk())
    		return false;
    	//	do it
    	if(reQuery) {
    		refresh(mGridTab.getKeys(), mGridTab.getKeyColumns(), true);
    	} else if(getTabLevel() > 0) {
    		int[] currentParent_Record_ID = Env.getTabRecord_ID(
        			getActivityNo(), getParentTabNo());
        	if(mGridTab.getParent_Record_ID() != currentParent_Record_ID[0]) {
        		refresh(0, true);
        	}
    	} else {
    		//mGridTab.loadData();
    	}
    	//	
    	changeMenuView();
    	//	Return
    	return ok;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	//	Valid is Loaded
    	if(!isLoadOk())
    		return;
    	//	
    	if(requestCode == ACTION_TAKE_PHOTO) {
    		new SaveTask().execute(PHOTO_ATTACHMENT_SAVE);
    	} else if(requestCode == ACTION_TAKE_FILE) {
    		//	Valid
    		if(data == null)
    			return;
    		//	
    		new SaveTask().execute(FILE_ATTACHMENT_SAVE, data.getData().getPath());
    	} else if (resultCode == Activity.RESULT_OK) {
	    	if(data != null) {
	    		Bundle bundle = data.getExtras();
	    		//	Item
	    		DisplayRecordItem item = (DisplayRecordItem) bundle.getParcelable("Record");
	    		switch (bundle.getInt(DisplayMenuItem.CONTEXT_ACTIVITY_TYPE)) {
				case DisplayMenuItem.CONTEXT_ACTIVITY_TYPE_Form:
					break;
				case DisplayMenuItem.CONTEXT_ACTIVITY_TYPE_Window:
					break;
				case DisplayMenuItem.CONTEXT_ACTIVITY_TYPE_Process:
					String summary = bundle.getString("Summary");
					boolean isError = bundle.getBoolean("IsError");
					//	Is a Error
					if(isError) {
						Msg.alertMsg(getActivity(), summary);
					} else {
						if(summary != null
								&& summary.length() > 0)
							Msg.toastMsg(getActivity(), summary);
					}
					//	Refresh
					refreshFromChange(true);
					break;
				case DisplayMenuItem.CONTEXT_ACTIVITY_TYPE_Report:
					break;
				case DisplayMenuItem.CONTEXT_ACTIVITY_TYPE_SearchWindow:
	    			//	Refresh
	    			int[] keys = item.getKeys();
	    			//	Verify
	    			if(keys[0] > 0)
	    				refresh(keys, item.getKeyColumns(), false);
	    			else
	    				newOption();
	    			//	
					break;
				case DisplayMenuItem.CONTEXT_ACTIVITY_TYPE_SearchColumn:
					String columnName = bundle.getString("ColumnName");
		    		//	if a field or just search
		    		if(columnName != null) {
		    			for (GridField vField: mGridTab.getFields()) {
		    	    		if(vField.getColumnName().equals(columnName)) {
		    	    			((VLookupSearch) vField).setItem(item);
		    	    			break;
		    	    		}
		    			}
		    		}
					break;
				default:
					break;
	    		}
	    	}
    	}
    }
    
    @Override
    public boolean save() {
    	return mGridTab.save();
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return false;
	}

	@Override
	public void onItemSelected(int [] record_ID, String [] keyColumns) {
		//	refresh
		if(isLoadOk())
			refresh(record_ID, keyColumns, false);	
	}

	@Override
	public void setTabParameter(TabParameter tabParam) {
		super.setTabParameter(tabParam);
		//	Initial Load
		if(!isLoadOk())
			initLoad();
	}
	
	@Override
	public boolean isModifying() {
		//	Initial Load
		if(!isLoadOk())
			return false;
		//	
		if(getTabLevel() > 0) {
			return false;
		}
		//	Default
		return super.isModifying();
	}

	@Override
	public void setIsParentModifying(boolean isParentModifying) {
		//	Initial Load
		if(!isLoadOk())
			return;
		//	
		if(getTabLevel() > 0) {
			super.setIsParentModifying(isParentModifying);
		} else {
			super.setIsParentModifying(false);
		}
	}
	
	/**********************************************************************
	 * Threads
	 */
	
	/**
	 * Include Class
	 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
	 *
	 */
	private class LoadViewTask extends AsyncTask<TableLayout, Integer, Integer> {

		/**	Layout					*/
		private LinearLayout		v_row			= null;
		private LayoutParams		v_param			= null;
		private LayoutParams		v_rowParam		= null;
		private TableLayout 		v_view 			= null;
		private ArrayList<Lookup>	m_Lookup 		= null;
		private int 				m_currentLookup = 0;
		/**	Progress Bar			*/
		private ProgressDialog 		v_PDialog;
		/**	Constant				*/
		private static final float 	WEIGHT_SUM 		= 2;
		private static final float 	WEIGHT 			= 1;
		/**
		 * Init Values
		 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com 17/05/2014, 12:18:42
		 * @return void
		 */
		private void init() {
	    	//	Set Parameter
	    	v_param = new LayoutParams(LayoutParams.MATCH_PARENT, 
	    			LayoutParams.MATCH_PARENT, WEIGHT);
	    	//	Set Parameter to Row
	    	v_rowParam = new LayoutParams(LayoutParams.MATCH_PARENT, 
	    			(int)getResources().getDimension(R.dimen.row_layout_height));
	    	//	Load Table Info
	    	tabInfo = new InfoTab(getActivity(), getSPS_Tab_ID(), conn);
	    	//	View
	    	mGridTab = new GridTab(getActivity(), getTabParameter(), tabInfo, conn);
	    	//	
	    	v_PDialog.setMax(tabInfo.getLength());
	    	m_Lookup = new ArrayList<Lookup>();
		}
		
		@Override
		protected void onPreExecute() {
			v_PDialog = ProgressDialog.show(getActivity(), null, 
					getString(R.string.msg_Loading), true, false);
		}
		
		@Override
		protected Integer doInBackground(TableLayout... params) {
			v_view = params[0];
			init();
			for(InfoField field : tabInfo.getFields()) {
				if(field.IsDisplayed
						&& (field.DisplayType == DisplayType.TABLE_DIR 
						|| field.DisplayType == DisplayType.LIST
						|| field.DisplayType == DisplayType.TABLE)) {
					//	Add View to Layout
					Lookup lookup = new Lookup(getActivity(), getTabParameter(), field);
					lookup.load();
					//	Add
					m_Lookup.add(lookup);
				}
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Integer... progress) {
			
		}

		@Override
		protected void onPostExecute(Integer result) {
			m_IsLoadDataOk = loadView();
			m_IsLoadDataOk = mGridTab.loadData();
			//	Modifying
			if(mGridTab.getRecord_ID() <= 0)
				setIsModifying(true);
			//	
			changeMenuView();
			v_PDialog.dismiss();
		}
		
	    /**
	     * Load View Objects
	     * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com 18/02/2014, 16:37:56
	     * @return
	     * @return boolean
	     */
	    protected boolean loadView() {
	    	boolean ok = false;
	    	//	
			try {
				//	Add Fields
		    	for(InfoField field : tabInfo.getFields()) {
		    		if(!field.IsDisplayed)
		    			continue;
		    		//	Add View to Layout
		    		addView(field);
		    	}
		    	//	Ok
		    	ok = true;
			} catch(Exception e) {
				LogM.log(getActivity(), getClass(), Level.SEVERE, e.getLocalizedMessage());
				//	Message
				Msg.alertMsg(getActivity(), 
						getString(R.string.msg_Error) + ": " + e.getLocalizedMessage());
			}
			return ok;
	    }
	 
	    /**
	     * Add to view
	     * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com 18/02/2014, 20:45:54
	     * @param field
	     * @return void
	     */
	    private void addView(InfoField field) {
	    	//	
	    	boolean isSameLine = field.IsSameLine;
	    	boolean isFirst = false;
			GridField lookup = null;
	    	//	Add New Row
			isFirst = (v_row == null);
			//	Add
			if(DisplayType.isDate(field.DisplayType)) {
				lookup = new VLookupDateBox(getActivity(), field, getTabParameter());
			} else if(DisplayType.isText(field.DisplayType)) {
				VLookupString lookupString = new VLookupString(getActivity(), field, getTabParameter());
				lookup = lookupString;
			} else if(DisplayType.isNumeric(field.DisplayType)) {
				VLookupNumber lookupNumber = new VLookupNumber(getActivity(), field, getTabParameter());
				lookup = lookupNumber;
			} else if(DisplayType.isBoolean(field.DisplayType)) {
				lookup = new VLookupCheckBox(getActivity(), field, getTabParameter());
			} else if(DisplayType.isLookup(field.DisplayType)) {
				//	Table Direct
				if(field.DisplayType == DisplayType.TABLE_DIR
						|| field.DisplayType == DisplayType.LIST
						|| field.DisplayType == DisplayType.TABLE) {
					lookup = new VLookupSpinner(getActivity(), field, getTabParameter(), m_Lookup.get(m_currentLookup++));
				} else if(field.DisplayType == DisplayType.SEARCH 
						|| field.DisplayType == DisplayType.LOCATION
						|| field.DisplayType == DisplayType.LOCATOR
						|| field.DisplayType == DisplayType.ACCOUNT) {
					lookup = new VLookupSearch(getActivity(), field, getTabParameter());
				}
			} else if(field.DisplayType == DisplayType.BUTTON) {
				VLookupButton lookupButton = null;
				if(field.ColumnName.equals("DocAction")) {
					lookupButton = new VLookupButtonDocAction(getActivity(), field, (DocAction) mGridTab.getPO());
				} else if(field.ColumnName.equals("PaymentRule")) {
					//	Payment Rule Button
					lookupButton = new VLookupButtonPaymentRule(getActivity(), field, getTabParameter());
				} else {
					lookupButton = new VLookupButton(getActivity(), field, getTabParameter());
				}
				//	Set Parameters
				lookupButton.setTabParameter(getTabParameter());
				lookup = lookupButton;
			}
			//	is Filled
			if(lookup != null) {
				//	Add new Row
				if(isFirst 
						|| !isSameLine) {
					v_row = new LinearLayout(getActivity());
					v_row.setOrientation(LinearLayout.HORIZONTAL);
					v_row.setWeightSum(WEIGHT_SUM);
					v_row.setLayoutParams(v_rowParam);
				}
				//	Set Listener
				lookup.setOnFieldChangeListener(m_Listener);
				lookup.setLayoutParams(v_param);
				//	Add to Row
				v_row.addView(lookup);
				//	
				mGridTab.addField(lookup);
				//	Add Row
				if(isFirst
						|| !isSameLine)
					v_view.addView(v_row);
			}
	    }
	}
	
	/**
	 * Save data in thread
	 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
	 *
	 */
	private class SaveTask extends AsyncTask<String, Void, Void> {

		/**	Progress Bar			*/
		private ProgressDialog 		v_PDialog;
		private boolean				is_OK 	= false;
		private String				m_Type 	= null;
		
		@Override
		protected void onPreExecute() {
			v_tableLayout.requestFocus();
			v_PDialog = ProgressDialog.show(getCallback(), null, 
					getString(R.string.msg_Saving), false, false);
			//	Set Max
		}
		
		@Override
		protected Void doInBackground(String... params) {
			m_Type = params[0];
			//	Valid Null
			if(m_Type == null)
				return null;
			//	
			if(m_Type.equals(PHOTO_ATTACHMENT_SAVE)) {
				String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
				m_AttHandler.processImgAttach(fileName);
			} else if(m_Type.equals(FILE_ATTACHMENT_SAVE)) { 
				String origFile = params[1];
				m_AttHandler.processFileAttach(origFile);
			} else if(m_Type.equals(RECORD_SAVE)) {
				is_OK = save();
			}
			//	
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Void... progress) {
			
		}

		@Override
		protected void onPostExecute(Void result) {
			//	For Record Save
			if(m_Type.equals(RECORD_SAVE)) {
				if(is_OK) {
					refreshIndex();
					m_IsLoadDataOk = refresh(mGridTab.getKeys(), mGridTab.getKeyColumns(), false);
					lockView(SEE);
				} else {
					Msg.alertMsg(getActivity(), mGridTab.getError());
				}
			}
			//	Hide
			v_PDialog.dismiss();
		}
	}

	@Override
	public String getTabSuffix() {
		return null;
	}
}