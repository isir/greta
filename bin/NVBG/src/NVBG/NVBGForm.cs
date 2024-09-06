using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Threading;
using System.Runtime.InteropServices;
using System.IO;


namespace NVBG
{
    delegate void StringParameterDelegate(string value);   
    delegate void SaliencyMapParameterDelegate(List<SaliencyItem> salMap, int randRange, int keywordRange);
    public delegate void MessageHandlerDelegate();
    public delegate void NVBGSetOptionCallback(string _charName, string _option, string _optionValue);
    public delegate void IdleTimerValueChangeCallback(string _characterName, string _value);
    public delegate void IdleTimerEnableCallback(string _characterName, bool _enable);
    public delegate void SaccadeCheckBox(bool _checked);    
    public delegate void GUILabelUpdate(string _characterName, string type, bool _checked);
    public delegate void RefreshGUI(string _characterName);

    public partial class NVBGForm : Form
    {
        List<string> m_commLineArgs;
        NVBGManager m_nvbg;       
        string m_logText;
        List<SaliencyItem> m_saliencyMap;
        int m_idleGazeRandomRange = 0;
        int m_keywordGazeRandomRange = 0;
        private System.Windows.Forms.Timer m_logTimer;
        private const int m_logUpdateTime = 1;
        private bool m_showSaliencyMap = true;
        public static bool writelog = false;
        

        public NVBGForm(List<string> commandLineArguments)
        {
            if (string.IsNullOrEmpty(Thread.CurrentThread.Name))
                Thread.CurrentThread.Name = "NVBGForm";

            m_commLineArgs = commandLineArguments;
            InitializeComponent();            
            this.Closing += new System.ComponentModel.CancelEventHandler(this.OnExit);
            m_logTimer = new System.Windows.Forms.Timer();

            // Initialize the log timer
            m_logTimer.Interval = m_logUpdateTime * 1000;
            m_logTimer.Tick += new EventHandler(UpdateLogText);
            m_logTimer.Enabled = true;
            m_logTimer.Start();

            for (int i = 0; i < commandLineArguments.Count; ++i)
            {
                if (commandLineArguments[i].Trim().Equals("-hide_GUI") && (commandLineArguments[i+1].Trim().Equals("true")))
                {
                    HideGUI();
                }
				if (commandLineArguments[i].Trim().Equals("-writelog"))
                {
					if(File.Exists(@"log.txt")) File.Delete(@"log.txt");
                    writelog=true;
                }
            }
        }

        public void HideGUI()
        {            
            AllBehaviorButton.Enabled = false;            
            SaliencyIdleGazeButton.Enabled = false;
            ClearLogButton.Enabled = false;
            LogText.Enabled = false;
            this.Opacity = 0;
            this.ShowInTaskbar = false;            
        }
       
        /// <summary>
        /// Spawns a new thread that creates and runs the Non Verbal Behavior Generator
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void NVBGForm_Load(object sender, EventArgs e)
        {              
            BeginNVBG();
        }

        /// <summary>
        /// used for initializing the checkbox for saccades if specified through command line (-saccades off/on)
        /// </summary>
        /// <param name="_checked"></param>
        private void InitializeSaccadeCheckbox(bool _checked)
        {
            this.useSaccadeCheckBox.Checked = _checked;
            NVBGSaliencyMap.m_useSaccade = _checked;
        }

        /// <summary>
        /// used to initialize all behavior through commandline
        /// </summary>
        /// <param name="_checked"></param>
        private void GUIUpdateLabel(string _characterName, string type, bool _checked)
        {


            if (InvokeRequired)
            {
                object[] args = new object[3];
                args[0] = _characterName;
                args[1] = type;
                args[2] = _checked;
                BeginInvoke(new GUILabelUpdate(GUIUpdateLabel), args);

                return;
            }


            if (_characterName.Equals(this.NameOfAgentLabel.Text) || (m_nvbg.m_characters.Count == 1))
            {
                switch (type)
                {
                    case "all_behavior":
                        this.AllBehaviorLabel.Text = _checked ? "on" : "off";
                        break;
                    case "saliency_glance":
                        this.SaliencyGlanceLabel.Text = _checked ? "on" : "off";
                        break;
                    case "saliency_idle_gaze":
                        this.SaliencyIdleGazeLabel.Text = _checked ? "on" : "off";
                        break;
                    case "speaker_gaze":
                        this.SpeakerGazeLabel.Text = _checked ? "on" : "off";
                        break;
                    case "speaker_gesture":
                        this.SpeakerGesturesLabel.Text = _checked ? "on" : "off";
                        break;
                    case "listener_gaze":
                        this.ListenerGazeLabel.Text = _checked ? "on" : "off";
                        break;
                    case "nvbg_POS_rules":
                        this.POSLabel.Text = _checked ? "on" : "off";
                        break;
                    case "update_character_name":
                        try
                        {
                            this.NameOfAgentLabel.Text = _characterName;
                        }
                        catch (Exception e)
                        {
                            NVBGLogger.Log(e.ToString());
                        }
                        break;
                    default:
                        break;
                }
            }        
        }



        /// <summary>
        /// creates an instance of NVBGManager and invokes the Run method
        /// </summary>
        private void BeginNVBG()
        {
            NVBGLogger.m_callback = new StringParameterDelegate(AppendTextToLog);
            NVBGSaliencyMap.m_callback = new SaliencyMapParameterDelegate(StoreSaliencyMap);
            NVBGManager.m_callbackHandler = new MessageHandlerDelegate(HandleMessage);            
            NVBGManager.m_idleTimerValueChangeHandler = new IdleTimerValueChangeCallback(IdleTimerValueChanged);
            NVBGManager.m_idleTimerEnableHandler = new IdleTimerEnableCallback(IdleTimerEnable);
            NVBGSaliencyMap.m_storyPointCallBack = new StringParameterDelegate(UpdateStoryPointComboBox);
            NVBGSaliencyMap.m_emotionCallBack = new StringParameterDelegate(UpdateEmotionComboBox);
            NVBGManager.m_saccadeInitializeHandler = new SaccadeCheckBox(InitializeSaccadeCheckbox);            
            NVBGManager.m_GUILabelUpdateHandler = new GUILabelUpdate(GUIUpdateLabel);
            NVBGManager.m_refreshGUIHandler = new RefreshGUI(RefreshGUIforCharacter);
            this.NameOfAgentLabel.Text = "None";
            m_nvbg = new NVBGManager();
            m_nvbg.ParseArguments(m_commLineArgs);

            if (m_nvbg.m_characters.Count > 0)
                this.NameOfAgentLabel.Text = m_nvbg.m_characters.ElementAt(0).Key.Trim();
            else
                this.NameOfAgentLabel.Text = "None";
            
            m_nvbg.Initialize();


            // Saliency Map GUI stuff
            if (NVBGSaliencyMap.m_idleGazeThresholdNumTypeNum)
            {
                this.thresholdComboBox.SelectedItem = this.thresholdComboBox.Items[0];
                this.thresholdTextBox.Text = NVBGSaliencyMap.m_idleGazeThresholdNum.ToString(); 
            }
            else
            {
                this.thresholdComboBox.SelectedItem = this.thresholdComboBox.Items[1];
                this.thresholdTextBox.Text = NVBGSaliencyMap.m_idleGazeThresholdValue.ToString();
            }
            if (NVBGSaliencyMap.m_keywordGazeThresholdTypeNum)
            {
                this.keywordThresholdComboBox.SelectedItem = this.keywordThresholdComboBox.Items[0];
                this.keywordThresholdTextBox.Text = NVBGSaliencyMap.m_keywordGazeThresholdNum.ToString();
            }
            else
            {
                this.keywordThresholdComboBox.SelectedItem = this.keywordThresholdComboBox.Items[1];
                this.keywordThresholdTextBox.Text = NVBGSaliencyMap.m_keywordGazeThresholdValue.ToString();
            }
            this.priorityFormulaTextBox.Text = NVBGSaliencyMap.m_priorityFormula;
            this.idleThresholdBiasTrackBar.Value = NVBGSaliencyMap.m_bias;
            this.idleThresholdBiasTextBox.Text = NVBGSaliencyMap.m_bias.ToString();
            this.periodTrackBar.Value = NVBGSaliencyMap.m_idleGazePeriod;
            this.periodTextBox.Text = NVBGSaliencyMap.m_idleGazePeriod.ToString();
            this.recencyAfterTextBox.Text = NVBGSaliencyMap.m_recencyAfterGaze.ToString();
            this.gazeTypeComboBox.Text = NVBGSaliencyMap.m_jointRange;
            if (NVBGSaliencyMap.m_jointRangeThresholdTypeNum)
            {
                this.jointRangeThresholdComboBox.SelectedItem = this.thresholdComboBox.Items[0];
                int id = getJointRangeId(this.gazeTypeComboBox.SelectedItem.ToString());
                this.jointRangeThresholdTextBox.Text = NVBGSaliencyMap.m_jointRangeThresholdNum[id].ToString();
            }
            else
            {
                this.jointRangeThresholdComboBox.SelectedItem = this.thresholdComboBox.Items[1];
                int id = getJointRangeId(this.gazeTypeComboBox.SelectedItem.ToString());
                this.jointRangeThresholdTextBox.Text = NVBGSaliencyMap.m_jointRangeThresholdValue[id].ToString();
            }
            this.rateTrackBar.Value = Convert.ToInt32(NVBGSaliencyMap.m_currentJointRangeRate * 100);
            this.rateTextBox.Text = NVBGSaliencyMap.m_currentJointRangeRate.ToString();
            if (NVBGSaliencyMap.m_fixedJointRange)
            {
                this.fixedJointRangeCheckBox.Checked = true;
            }
            int jointNameId = getJointNameId(this.jointNameComboBox.SelectedItem.ToString());
            this.pitchUpTrackBar.Value = -NVBGSaliencyMap.m_jointPitchUp[jointNameId];
            this.pitchUpTextBox.Text = NVBGSaliencyMap.m_jointPitchUp[jointNameId].ToString();
            this.pitchDownTrackBar.Value = NVBGSaliencyMap.m_jointPitchDown[jointNameId];
            this.pitchDownTextBox.Text = NVBGSaliencyMap.m_jointPitchDown[jointNameId].ToString();
            this.headingTrackBar.Value = NVBGSaliencyMap.m_jointHeading[jointNameId];
            this.headingTextBox.Text = NVBGSaliencyMap.m_jointHeading[jointNameId].ToString();
            this.rollTrackBar.Value = NVBGSaliencyMap.m_jointRoll[jointNameId];
            this.rollTextBox.Text = NVBGSaliencyMap.m_jointRoll[jointNameId].ToString();
            if (NVBGSaliencyMap.m_useSaccade)
            {
                this.useSaccadeCheckBox.Checked = true;
            }
            this.radiusTrackBar.Value = NVBGSaliencyMap.m_saccadeMagnitude;
            this.radiusTextBox.Text = NVBGSaliencyMap.m_saccadeMagnitude.ToString();
            this.saccadePeriodTrackBar.Value = Convert.ToInt32(NVBGSaliencyMap.m_saccadePeriod * 100);
            this.saccadePeriodTextBox.Text = NVBGSaliencyMap.m_saccadePeriod.ToString();
            updatePersonality();
        }

        private void IdleTimerValueChanged(string _characterName, string _value)
        {

            if (m_nvbg.m_characters.Count == 0)
                return;

            if (InvokeRequired)
            {
                object[] args = new object[2];
                args[0] = _characterName;
                args[1] = _value;
                BeginInvoke(new IdleTimerValueChangeCallback(IdleTimerValueChanged),args);

                return;
            }

            m_nvbg.SetIdleTimerInterval(_characterName, (float)(Convert.ToDouble(_value)));
            try
            {
                this.periodTrackBar.Value = (Convert.ToInt32(_value) * 10);
                this.periodTextBox.Text = _value;
            }
            catch (Exception e)
            {
                NVBGLogger.Log(e.ToString());
            }
        }

        private void IdleTimerEnable(string _characterName, bool _enable)
        {
            if (InvokeRequired)
            {
                object[] args = new object[2];
                args[0] = _characterName;
                args[1] = _enable;
                BeginInvoke(new IdleTimerEnableCallback(IdleTimerEnable), args);
                return;
            }
            m_nvbg.SetIdleTimerEnable(_characterName, _enable);
        }

        private void HandleMessage()
        {

            if (InvokeRequired)
            {
                BeginInvoke(new MessageHandlerDelegate(HandleMessage));
                return;
            }

            m_nvbg.ReactToInputMessage();
        }

        /// <summary>
        /// Callback method for updating log in textbox
        /// </summary>
        /// <param name="_logText"></param>
        public void AppendTextToLog(string _logText)
        {

            object[] parameter = new object[1];
            parameter[0] = _logText;
            if (InvokeRequired)
            {
                BeginInvoke(new StringParameterDelegate(AppendTextToLog), parameter);
                return;
            }

            m_logText += _logText;

        }

        public void StoreSaliencyMap(List<SaliencyItem> _salMap, int _randRange, int _keywordRange)
        {
            m_saliencyMap = _salMap;
            m_idleGazeRandomRange = _randRange;
            m_keywordGazeRandomRange = _keywordRange;
        }

        private void UpdateLogText(object sender, EventArgs eArgs)
        {            

            bool restore = false;
            int start = 0;
            int length = 0;
            try
            {
                if (LogText.SelectionStart != LogText.TextLength)
                {
                    restore = true;
                    start = LogText.SelectionStart;
                    length = LogText.SelectionLength;
                }
            }
            catch (Exception e)
            {
                NVBGLogger.Log(e.ToString());
            }
            try
            {
                LogText.Text += m_logText;
            }
            catch (Exception e)
            {
                NVBGLogger.Log(e.ToString());
            }
            m_logText = "";

            if (restore)
            {
                LogText.Select(start, length);
                //LogText.ScrollToCaret();
            }
            else
            {

                if (System.Environment.OSVersion.ToString().Contains("Windows"))
                {
                    try
                    {
                        Win32Interop.SendMessage(new HandleRef(LogText, LogText.Handle), Win32Interop.WM_VSCROLL, new IntPtr(Win32Interop.SB_BOTTOM), new IntPtr(0));
                    }
                    catch (Exception e)
                    {
                        NVBGLogger.Log(e.ToString());
                    }
                }
            }
            
        }

        private void UpdateSaliencyMapDataGridView(object sender, EventArgs eArgs)
        {
            try
            {
                if (!NVBGManager.m_isHelp && SaliencyMap.m_isInitialized)
                {
                    while (SaliencyMapDataGridView.Rows.Count > 0)
                    {
                        SaliencyMapDataGridView.Rows.RemoveAt(0);
                    }
                    for (int i = 0; i < m_saliencyMap.Count; ++i)
                    {
                        string[] row = { m_saliencyMap[i].objectName, m_saliencyMap[i].primacy.ToString(), m_saliencyMap[i].recency.ToString(), m_saliencyMap[i].priority.ToString() };
                        SaliencyMapDataGridView.Rows.Add(row);
                    }
                    for (int i = 0; i < this.SaliencyMapDataGridView.Rows.Count; ++i)
                    {
                        this.SaliencyMapDataGridView.Rows[i].DefaultCellStyle.BackColor = Color.White;
                    }
                    for (int i = 0; i < m_idleGazeRandomRange; ++i)
                    {
                        this.SaliencyMapDataGridView.Rows[i].DefaultCellStyle.BackColor = Color.Yellow;
                    }
                    for (int i = 0; i < m_keywordGazeRandomRange; ++i)
                    {
                        if (i < m_idleGazeRandomRange)
                        {
                            this.SaliencyMapDataGridView.Rows[i].DefaultCellStyle.BackColor = Color.YellowGreen;
                        }
                        else
                        {
                            this.SaliencyMapDataGridView.Rows[i].DefaultCellStyle.BackColor = Color.Green;
                        }
                    }
                }
            }
            catch (Exception)
            {
                // do nothing
                //string temp = e.ToString();
            }
        }

        private void UpdateStoryPointComboBox(string storyPoint)
        {
            if (InvokeRequired)
            {
                object[] args = new object[1];
                args[0] = storyPoint;
                BeginInvoke(new StringParameterDelegate(UpdateStoryPointComboBox), args);

                return;
            }
            this.storyPointComboBox.Text = storyPoint;
        }

        private void UpdateEmotionComboBox(string emotion)
        {
            if (InvokeRequired)
            {
                object[] args = new object[1];
                args[0] = emotion;
                BeginInvoke(new StringParameterDelegate(UpdateEmotionComboBox),args);

                return;
            }
            this.emotionComboBox.Text = emotion;
        }

        private void OnExit(object sender, System.ComponentModel.CancelEventArgs e)
        {
            //NVBGManager.m_vhmsg.SendMessage("vrKillComponent nvb");
            m_nvbg.CloseActiveMQ();
        }

        private void ClearLogButton_Click(object sender, EventArgs e)
        {
            LogText.Text = "";
            m_logText = "";
        }        

        private void AllBehaviorButton_Click(object sender, EventArgs e)
        {

            if (m_nvbg.m_characters.Count == 0)
                return;

            lock (NVBGManager.m_switchLock)
            {
                if (AllBehaviorLabel.Text.Equals("On"))
                {
                    AllBehaviorLabel.Text = "Off";
                    m_nvbg.m_characters[this.NameOfAgentLabel.Text].Switch.allBehaviour = false;
                }
                else
                {
                    AllBehaviorLabel.Text = "On";
                    m_nvbg.m_characters[this.NameOfAgentLabel.Text].Switch.allBehaviour = true;
                }
            }
        }

        private void SaliencyGazeButton_Click(object sender, EventArgs e)
        {

            if (m_nvbg.m_characters.Count == 0)
                return;

            lock (NVBGManager.m_switchLock)
            {
                if (SaliencyIdleGazeLabel.Text.Equals("On"))
                {
                    SaliencyIdleGazeLabel.Text = "Off";
                    m_nvbg.m_characters[this.NameOfAgentLabel.Text].Switch.saliencyIdleGaze = false;

                }
                else
                {
                    SaliencyIdleGazeLabel.Text = "On";
                    m_nvbg.m_characters[this.NameOfAgentLabel.Text].Switch.saliencyIdleGaze = true;
                }
            }
        }

        private void saliencyMapShowButton_Click(object sender, EventArgs e)
        {            

            m_showSaliencyMap = !m_showSaliencyMap;
            if (m_showSaliencyMap)
            {
                this.Width = 1030;
                this.saliencyMapShowButton.Text = "Saliency Map <<<";
            }
            else
            {
                this.Width = 557;
                this.saliencyMapShowButton.Text = "Saliency Map >>>";
            }
        }

        private void thresholdComboBox_SelectIndexChanged(object sender, EventArgs e)
        { 
            if (this.thresholdComboBox.SelectedItem.ToString() == "num")
            { 
                NVBGSaliencyMap.m_idleGazeThresholdNumTypeNum = true;
                this.thresholdTextBox.Text = NVBGSaliencyMap.m_idleGazeThresholdNum.ToString();
            }
            else if (this.thresholdComboBox.SelectedItem.ToString() == "value")
            {
                NVBGSaliencyMap.m_idleGazeThresholdNumTypeNum = false;
                this.thresholdTextBox.Text = NVBGSaliencyMap.m_idleGazeThresholdValue.ToString();
            }
        }

        private void thresholdTextBox_KeyPressed(object sender, KeyPressEventArgs e)
        { 
            if (e.KeyChar == (char)13)
            {
                if (this.thresholdComboBox.SelectedItem.ToString() == "num")
                { 
                    NVBGSaliencyMap.m_idleGazeThresholdNumTypeNum = true;
                    NVBGSaliencyMap.m_idleGazeThresholdNum = int.Parse(this.thresholdTextBox.Text);
                }
                else if (this.thresholdComboBox.SelectedItem.ToString() == "value")
                {
                    NVBGSaliencyMap.m_idleGazeThresholdNumTypeNum = false;
                    NVBGSaliencyMap.m_idleGazeThresholdValue = float.Parse(this.thresholdTextBox.Text);
                }
            }
        }

        private void thresholdBiasTrackBar_Scroll(object sender, EventArgs e)
        {
            NVBGSaliencyMap.m_bias = this.idleThresholdBiasTrackBar.Value;
            this.idleThresholdBiasTextBox.Text = this.idleThresholdBiasTrackBar.Value.ToString();
        }

        private void keywordThresholdComboBox_SelectIndexChanged(object sender, EventArgs e)
        {
            if (this.keywordThresholdComboBox.SelectedItem.ToString() == "num")
            {
                NVBGSaliencyMap.m_keywordGazeThresholdTypeNum = true;
                this.keywordThresholdTextBox.Text = NVBGSaliencyMap.m_keywordGazeThresholdNum.ToString();
            }
            else if (this.keywordThresholdComboBox.SelectedItem.ToString() == "value")
            {
                NVBGSaliencyMap.m_keywordGazeThresholdTypeNum = false;
                this.keywordThresholdTextBox.Text = NVBGSaliencyMap.m_keywordGazeThresholdValue.ToString();
            }
        }

        private void keywordThresholdTextBox_KeyPressed(object sender, KeyPressEventArgs e)
        {
            if (e.KeyChar == (char)13)
            {
                if (this.keywordThresholdComboBox.SelectedItem.ToString() == "num")
                {
                    NVBGSaliencyMap.m_keywordGazeThresholdTypeNum = true;
                    NVBGSaliencyMap.m_keywordGazeThresholdNum = int.Parse(this.keywordThresholdTextBox.Text);
                }
                else if (this.keywordThresholdComboBox.SelectedItem.ToString() == "value")
                {
                    NVBGSaliencyMap.m_keywordGazeThresholdTypeNum = false;
                    NVBGSaliencyMap.m_keywordGazeThresholdValue = float.Parse(this.keywordThresholdTextBox.Text);
                }
            }
        }

        private void priorityTextBox_KeyPressed(object sender, KeyPressEventArgs e)
        {
            if (e.KeyChar == (char)13)
            {
                NVBGSaliencyMap.m_priorityFormula = this.priorityFormulaTextBox.Text;
            }
        }

        private void periodTrackBar_Scroll(object sender, System.EventArgs e)
        {
            if (m_nvbg.m_characters.Count == 0)
                return;

            this.m_nvbg.SetIdleTimerInterval(m_nvbg.m_characters[this.NameOfAgentLabel.Text].AgentInfo.Name, (float)this.periodTrackBar.Value / 10);
            this.periodTextBox.Text = ((float)this.periodTrackBar.Value / 10).ToString();
        }

        private void RecencyAfterTextBox_KeyPressed(object sender, KeyPressEventArgs e)
        {
            if (e.KeyChar == (char)13)
            {
                NVBGSaliencyMap.m_recencyAfterGaze = float.Parse(this.recencyAfterTextBox.Text);
            }
        }

        private void gazeTypeComboBox_SelectIndexChanged(object sender, System.EventArgs e)
        {
            NVBGSaliencyMap.m_jointRange = this.gazeTypeComboBox.SelectedItem.ToString();
        }

        private void setPrimacyButton_Clicked(object sender, System.EventArgs e)
        {

            if (m_nvbg.m_characters.Count == 0)
                return;

            if (this.pawnNameTextBox.Text != "" && this.primacyTextBox.Text != "") {
                string outputMessage = "SaliencyMapPawnPrimacy " + m_nvbg.m_characters[this.NameOfAgentLabel.Text].AgentInfo.Name + " "
                    + this.pawnNameTextBox.Text + " " + this.primacyTextBox.Text;
                try
                {
                    NVBGManager.m_vhmsg.SendMessage(outputMessage);
                }
                catch (Exception exc)
                {
                    NVBGLogger.Log(exc.ToString());
                }
                NVBGLogger.Log(outputMessage);
            }
        }

        private void jointNameComboBox_SelectIndexChanged(object sender, System.EventArgs e)
        {
            int id = getJointNameId(this.jointNameComboBox.SelectedItem.ToString());
            pitchUpTrackBar.Value = -NVBGSaliencyMap.m_jointPitchUp[id];
            pitchDownTrackBar.Value = NVBGSaliencyMap.m_jointPitchDown[id];
            headingTrackBar.Value = NVBGSaliencyMap.m_jointHeading[id];
            rollTrackBar.Value = NVBGSaliencyMap.m_jointRoll[id];
            pitchUpTextBox.Text = NVBGSaliencyMap.m_jointPitchUp[id].ToString();
            pitchDownTextBox.Text = NVBGSaliencyMap.m_jointPitchDown[id].ToString();
            headingTextBox.Text = NVBGSaliencyMap.m_jointHeading[id].ToString();
            rollTextBox.Text = NVBGSaliencyMap.m_jointRoll[id].ToString();
        }

        private int getJointNameId(string s)
        {
            int id = 0;
            switch (s.ToString())
            {
                case "eyes":
                    id = (int)NVBGSaliencyMap.jointName.eyes;
                    break;
                case "neck":
                    id = (int)NVBGSaliencyMap.jointName.neck;
                    break;
                case "chest":
                    id = (int)NVBGSaliencyMap.jointName.chest;
                    break;
                case "back":
                    id = (int)NVBGSaliencyMap.jointName.back;
                    break;
                default:
                    id = (int)NVBGSaliencyMap.jointName.eyes;
                    break;
            }
            return id;
        }

        private string getJointNameString(int id)
        {
            string s = "";
            switch (id)
            { 
                case (int)NVBGSaliencyMap.jointName.eyes:
                    s = "eyes";
                    break;
                case (int)NVBGSaliencyMap.jointName.neck:
                    s = "neck";
                    break;
                case (int)NVBGSaliencyMap.jointName.chest:
                    s = "chest";
                    break;
                case (int)NVBGSaliencyMap.jointName.back:
                    s = "back";
                    break;
                default:
                    s = "eyes";
                    break;
            }
            return s;
        }

        private void pitchUpTrackBar_Scroll(object sender, System.EventArgs e)
        {
            int id = getJointNameId(this.jointNameComboBox.SelectedItem.ToString());
            NVBGSaliencyMap.m_jointPitchUp[id] = -this.pitchUpTrackBar.Value;
            this.pitchUpTextBox.Text = (-this.pitchUpTrackBar.Value).ToString();
        }

        private void pitchDownTrackBar_Scroll(object sender, System.EventArgs e)
        {
            int id = getJointNameId(this.jointNameComboBox.SelectedItem.ToString());
            NVBGSaliencyMap.m_jointPitchDown[id] = this.pitchDownTrackBar.Value;
            this.pitchDownTextBox.Text = this.pitchDownTrackBar.Value.ToString();
        }

        private void headingTrackBar_Scroll(object sender, System.EventArgs e)
        {
            int id = getJointNameId(this.jointNameComboBox.SelectedItem.ToString());
            NVBGSaliencyMap.m_jointHeading[id] = this.headingTrackBar.Value;
            this.headingTextBox.Text = this.headingTrackBar.Value.ToString();
        }

        private void rollTrackBar_Scroll(object sender, System.EventArgs e)
        {
            int id = getJointNameId(this.jointNameComboBox.SelectedItem.ToString());
            NVBGSaliencyMap.m_jointRoll[id] = this.rollTrackBar.Value;
            this.rollTextBox.Text = this.rollTrackBar.Value.ToString();
        }

        private void setGazeLimitsButton_Clicked(object sender, System.EventArgs e)
        {
            string message = "sbm gazelimit ";
            message += this.jointNameComboBox.SelectedItem.ToString();
            message += " ";
            message += (-this.pitchUpTrackBar.Value).ToString();
            message += " ";
            message += this.pitchDownTrackBar.Value.ToString();
            message += " ";
            message += this.headingTrackBar.Value.ToString();
            message += " ";
            message += this.rollTrackBar.Value.ToString();
            try
            {
                NVBGManager.m_vhmsg.SendMessage(message);
            }
            catch (Exception ex)
            {
                NVBGLogger.Log(ex.ToString());
            }
            NVBGLogger.Log(message);
        }

        private int getJointRangeId(string jointRange)
        {
            int id = 0;
            switch (jointRange)
            { 
                case "EYE":
                    id = (int)NVBGSaliencyMap.jointRange.eye;
                    break;
                case "EYE NECK":
                    id = (int)NVBGSaliencyMap.jointRange.eyeNeck;
                    break;
                case "EYE CHEST":
                    id = (int)NVBGSaliencyMap.jointRange.eyeChest;
                    break;
                case "EYE BACK":
                    id = (int)NVBGSaliencyMap.jointRange.eyeBack;
                    break;
                default:
                    id = (int)NVBGSaliencyMap.jointRange.eye;
                    break;
            }
            return id;
        }

        private void jointRangeThresholdComboBox_SelectedIndexChanged(object sender, System.EventArgs e)
        {
            int id = getJointNameId(this.jointNameRangeComboBox.SelectedItem.ToString());
            if (this.jointRangeThresholdComboBox.SelectedItem.ToString().Equals("num"))
            {
                NVBGSaliencyMap.m_jointRangeThresholdTypeNum = true;
                this.jointRangeThresholdTextBox.Text = NVBGSaliencyMap.m_jointRangeThresholdNum[id].ToString();
            }
            else
            {
                NVBGSaliencyMap.m_jointRangeThresholdTypeNum = false;
                this.jointRangeThresholdTextBox.Text = NVBGSaliencyMap.m_jointRangeThresholdValue[id].ToString();
            }
        }

        private void fixedJointRange_CheckedChanged(object sender, System.EventArgs e)
        {
            if (this.fixedJointRangeCheckBox.Checked)
            {
                NVBGSaliencyMap.m_fixedJointRange = true;
            }
            else
            {
                NVBGSaliencyMap.m_fixedJointRange = false;
            }
        }

        private void jointNameRangeComboBox_SelectIndexChanged(object sender, System.EventArgs e)
        {
            int id = getJointNameId(this.jointNameRangeComboBox.SelectedItem.ToString());
            if (this.jointRangeThresholdComboBox.SelectedItem.ToString() == "num")
            {
                NVBGSaliencyMap.m_jointRangeThresholdTypeNum = true;
                this.jointRangeThresholdTextBox.Text = NVBGSaliencyMap.m_jointRangeThresholdNum[id].ToString();
            }
            else if (this.jointRangeThresholdComboBox.SelectedItem.ToString() == "value")
            {
                NVBGSaliencyMap.m_jointRangeThresholdTypeNum = false;
                this.jointRangeThresholdTextBox.Text = NVBGSaliencyMap.m_jointRangeThresholdValue[id].ToString();
            }
        }

        private void jointRangeTextBox_KeyPressed(object sender, KeyPressEventArgs e)
        {
            if (e.KeyChar == (char)13)
            {
                int id = getJointNameId(this.jointNameRangeComboBox.SelectedItem.ToString());
                if (this.jointRangeThresholdComboBox.SelectedItem.ToString() == "num")
                {
                    NVBGSaliencyMap.m_jointRangeThresholdTypeNum = true;
                    NVBGSaliencyMap.m_jointRangeThresholdNum[id] = int.Parse(this.jointRangeThresholdTextBox.Text);
                }
                else if (this.jointRangeThresholdComboBox.SelectedItem.ToString() == "value")
                {
                    NVBGSaliencyMap.m_jointRangeThresholdTypeNum = false;
                    NVBGSaliencyMap.m_jointRangeThresholdValue[id] = float.Parse(this.jointRangeThresholdTextBox.Text);
                }
            }
        }

        private void useSaccadeCheckBox_CheckedChanged(object sender, System.EventArgs e)
        {

            if (m_nvbg.m_characters.Count == 0)
                return;

            if (this.useSaccadeCheckBox.Checked)
            {
                NVBGSaliencyMap.m_useSaccade = true;
            }
            else {
                NVBGSaliencyMap.m_useSaccade = false;
                string message = "sbm char ";
                message += m_nvbg.m_characters[this.NameOfAgentLabel.Text].AgentInfo.Name;
                message += " <saccade finish=\"true\"/>";
                try
                {
                    NVBGManager.m_vhmsg.SendMessage(message);
                }
                catch (Exception ex)
                {
                    NVBGLogger.Log(ex.ToString());
                }
                NVBGLogger.Log(message);
            }
        }

        private void radiusTrackBar_Scroll(object sender, System.EventArgs e)
        {
            this.radiusTextBox.Text = this.radiusTrackBar.Value.ToString();
            NVBGSaliencyMap.m_saccadeMagnitude = this.radiusTrackBar.Value;
        }

        private void saccadePeriodTrackBar_Scroll(object sender, System.EventArgs e)
        {
            this.saccadePeriodTextBox.Text = ((float)this.saccadePeriodTrackBar.Value / 100).ToString();
            NVBGSaliencyMap.m_saccadePeriod = (float)this.saccadePeriodTrackBar.Value / 100;
        }

        private void personalityComboBox_SelectedIndexChanged(object sender, System.EventArgs e)
        {
            updatePersonality();
        }

        private void updatePersonality()
        {

            if (m_nvbg.m_characters.Count == 0)
                return;
            try
            {
                int id = 0;
                switch (this.personalityComboBox.SelectedItem.ToString())
                {
                    case "man":
                        id = (int)NVBGSaliencyMap.personality.man;
                        break;
                    case "woman":
                        id = (int)NVBGSaliencyMap.personality.woman;
                        break;
                    case "old":
                        id = (int)NVBGSaliencyMap.personality.old;
                        break;
                    case "child":
                        id = (int)NVBGSaliencyMap.personality.child;
                        break;
                    default:
                        id = (int)NVBGSaliencyMap.personality.man;
                        break;
                }
                // update joint limits
                int jointNum = 2;
                for (int i = 0; i < jointNum; ++i) // currently only update eye and neck limits
                {
                    string message = "sbm gazelimit ";
                    message += getJointNameString(i);
                    message += " ";
                    // ToDo: start changing here... 
                    message += NVBGSaliencyMap.m_jointPitchUpPer[id, i];
                    message += " ";
                    message += NVBGSaliencyMap.m_jointPitchDownPer[id, i];
                    message += " ";
                    message += NVBGSaliencyMap.m_jointHeadingPer[id, i];
                    message += " ";
                    message += NVBGSaliencyMap.m_jointRollPer[id, i];
                    try
                    {
                        NVBGManager.m_vhmsg.SendMessage(message);
                    }
                    catch (Exception ex)
                    {
                        NVBGLogger.Log(ex.ToString());
                    }
                    NVBGLogger.Log(message);

                    NVBGSaliencyMap.m_jointPitchUp[i] = NVBGSaliencyMap.m_jointPitchUpPer[id, i];
                    NVBGSaliencyMap.m_jointPitchDown[i] = NVBGSaliencyMap.m_jointPitchDownPer[id, i];
                    NVBGSaliencyMap.m_jointHeading[i] = NVBGSaliencyMap.m_jointHeadingPer[id, i];
                    NVBGSaliencyMap.m_jointRoll[i] = NVBGSaliencyMap.m_jointRollPer[id, i];
                }
                // update saccade parameters
                NVBGSaliencyMap.m_saccadeMagnitude = NVBGSaliencyMap.m_saccadeMagnitudePer[id];
                NVBGSaliencyMap.m_saccadePeriod = NVBGSaliencyMap.m_saccadePeriodPer[id];

                // update gaze period
                m_nvbg.SetIdleTimerInterval(m_nvbg.m_characters[this.NameOfAgentLabel.Text].AgentInfo.Name, NVBGSaliencyMap.m_gazePeriodPer[id]);
                this.periodTrackBar.Value = Convert.ToInt32(NVBGSaliencyMap.m_gazePeriodPer[id] * 10);
                this.periodTextBox.Text = NVBGSaliencyMap.m_gazePeriodPer[id].ToString();

                // updat GUI
                int jointNameId = getJointNameId(this.jointNameComboBox.SelectedItem.ToString());
                if (jointNameId < jointNum)
                {
                    this.pitchUpTrackBar.Value = -NVBGSaliencyMap.m_jointPitchUpPer[id, jointNameId];
                    this.pitchUpTextBox.Text = NVBGSaliencyMap.m_jointPitchUpPer[id, jointNameId].ToString();
                    this.pitchDownTrackBar.Value = NVBGSaliencyMap.m_jointPitchDownPer[id, jointNameId];
                    this.pitchDownTextBox.Text = NVBGSaliencyMap.m_jointPitchDownPer[id, jointNameId].ToString();
                    this.headingTrackBar.Value = NVBGSaliencyMap.m_jointHeadingPer[id, jointNameId];
                    this.headingTextBox.Text = NVBGSaliencyMap.m_jointHeadingPer[id, jointNameId].ToString();
                    this.rollTrackBar.Value = NVBGSaliencyMap.m_jointRollPer[id, jointNameId];
                    this.rollTextBox.Text = NVBGSaliencyMap.m_jointRollPer[id, jointNameId].ToString();
                }
                this.radiusTrackBar.Value = NVBGSaliencyMap.m_saccadeMagnitude;
                this.radiusTextBox.Text = NVBGSaliencyMap.m_saccadeMagnitude.ToString();
                this.saccadePeriodTrackBar.Value = Convert.ToInt32(NVBGSaliencyMap.m_saccadePeriod * 100);
                this.saccadePeriodTextBox.Text = NVBGSaliencyMap.m_saccadePeriod.ToString();
            }
            catch (Exception)
            {
                //string text = e.ToString();
            }
        }

        private void rateTrackBar_Scroll(object sender, EventArgs e)
        {
            NVBGSaliencyMap.m_currentJointRangeRate = (float)this.rateTrackBar.Value / 100;
            this.rateTextBox.Text = NVBGSaliencyMap.m_currentJointRangeRate.ToString();
        }

        private void storyPointComboBox_SelectedIndexChanged(object sender, System.EventArgs e)
        {
            NVBGSaliencyMap.m_storyPoint = this.storyPointComboBox.SelectedItem.ToString();
        }

        private void emotionComboBox_SelectedIndexChanged(object sender, System.EventArgs e)
        {

            if (m_nvbg.m_characters.Count == 0)
                return;

            string emotionMessage = "SaliencyMapEmotion " + m_nvbg.m_characters[this.NameOfAgentLabel.Text].AgentInfo.Name + " " + this.emotionComboBox.SelectedItem.ToString();
            try
            {
                NVBGManager.m_vhmsg.SendMessage(emotionMessage);
            }
            catch (Exception ex)
            {
                NVBGLogger.Log(ex.ToString());
            }
            NVBGLogger.Log(emotionMessage);
        }

        private void showSaccadeMessageButton_Click(object sender, System.EventArgs e)
        {
            NVBGSaliencyMap.m_showSaccadeMessage = !NVBGSaliencyMap.m_showSaccadeMessage;
            if (NVBGSaliencyMap.m_showSaccadeMessage)
            {
                this.showSaccadeMessageButton.Text = "Hide Saccade Message";
            }
            else
            {
                this.showSaccadeMessageButton.Text = "Show Saccade Message";
            }
        }

        private void label1_Click(object sender, EventArgs e)
        {

        }

        private void SaliencyGlancesButton_Click(object sender, EventArgs e)
        {

            if (m_nvbg.m_characters.Count == 0)
                return;

            if (SaliencyGlanceLabel.Text.Equals("On"))
            {
                SaliencyGlanceLabel.Text = "Off";
                m_nvbg.m_characters[this.NameOfAgentLabel.Text].Switch.saliencyGlance = false;

            }
            else
            {
                SaliencyGlanceLabel.Text = "On";
                m_nvbg.m_characters[this.NameOfAgentLabel.Text].Switch.saliencyGlance = true;
            }
        }

        private void SpeakerGesturesButton_Click(object sender, EventArgs e)
        {

            if (m_nvbg.m_characters.Count == 0)
                return;
            if (SpeakerGesturesLabel.Text.Equals("On"))
            {
                SpeakerGesturesLabel.Text = "Off";
                m_nvbg.m_characters[this.NameOfAgentLabel.Text].Switch.speakerGestures = false;

            }
            else
            {
                SpeakerGesturesLabel.Text = "On";
                m_nvbg.m_characters[this.NameOfAgentLabel.Text].Switch.speakerGestures = true;
            }
        }

        private void SpeakerGazeButton_Click(object sender, EventArgs e)
        {

            if (m_nvbg.m_characters.Count == 0)
                return;

            if (SpeakerGazeLabel.Text.Equals("On"))
            {
                SpeakerGazeLabel.Text = "Off";
                m_nvbg.m_characters[this.NameOfAgentLabel.Text].Switch.speakerGaze = false;

            }
            else
            {
                SpeakerGazeLabel.Text = "On";
                m_nvbg.m_characters[this.NameOfAgentLabel.Text].Switch.speakerGaze = true;
            }
        }

        private void ListenerGazeButton_Click(object sender, EventArgs e)
        {

            if (m_nvbg.m_characters.Count == 0)
                return;

            if (ListenerGazeLabel.Text.Equals("On"))
            {
                ListenerGazeLabel.Text = "Off";
                m_nvbg.m_characters[this.NameOfAgentLabel.Text].Switch.listenerGaze = false;

            }
            else
            {
                ListenerGazeLabel.Text = "On";
                m_nvbg.m_characters[this.NameOfAgentLabel.Text].Switch.listenerGaze = true;
            }
        }

        private void POSButton_Click(object sender, EventArgs e)
        {

            if (m_nvbg.m_characters.Count == 0)
                return;

            if (POSLabel.Text.Equals("On"))
            {
                POSLabel.Text = "Off";
                m_nvbg.m_characters[this.NameOfAgentLabel.Text].Switch.POSRules = false;

            }
            else
            {
                POSLabel.Text = "On";
                m_nvbg.m_characters[this.NameOfAgentLabel.Text].Switch.POSRules = true;
            }
        }

        private void selectCharacterButton_Click(object sender, EventArgs e)
        {

            if (m_nvbg.m_characters.Count == 0)
                return;

            if (m_nvbg.m_characters.Count > 1)
            {
                int i = 0;
                foreach (KeyValuePair<string, NVBGCharacter> entry in m_nvbg.m_characters)
                {
                    i++;
                    if (entry.Key.Equals(this.NameOfAgentLabel.Text))
                        break;                    
                }

                // loop back to start after last character
                i = i % m_nvbg.m_characters.Count;

                this.NameOfAgentLabel.Text = m_nvbg.m_characters.ElementAt(i).Key;
                RefreshGUIforCharacter(m_nvbg.m_characters.ElementAt(i).Key);
            }
        }

        void RefreshGUIforCharacter(string _characterName)
        {

            if (m_nvbg.m_characters.Count == 0)
                return;

            if (InvokeRequired)
            {
                object[] args = new object[1];
                args[0] = _characterName;
                BeginInvoke(new RefreshGUI(RefreshGUIforCharacter), args);

                return;
            }

            if (this.NameOfAgentLabel.Text.Equals(_characterName))
            {
                this.AllBehaviorLabel.Text = m_nvbg.m_characters[_characterName].Switch.allBehaviour ? "On" : "Off";
                this.SaliencyIdleGazeLabel.Text = m_nvbg.m_characters[_characterName].Switch.saliencyIdleGaze ? "On" : "Off";
                this.SaliencyGlanceLabel.Text = m_nvbg.m_characters[_characterName].Switch.saliencyGlance ? "On" : "Off";
                this.SpeakerGesturesLabel.Text = m_nvbg.m_characters[_characterName].Switch.speakerGestures ? "On" : "Off";
                this.SpeakerGazeLabel.Text = m_nvbg.m_characters[_characterName].Switch.speakerGaze ? "On" : "Off";
                this.ListenerGazeLabel.Text = m_nvbg.m_characters[_characterName].Switch.listenerGaze ? "On" : "Off";
                this.POSLabel.Text = m_nvbg.m_characters[_characterName].Switch.POSRules ? "On" : "Off";
            }
        }
        
    }

    


    public class Win32Interop
    {
        public const int SB_BOTTOM = 0x07;
        public const int WM_VSCROLL = 0x0115;


        [DllImport("user32.dll", CharSet = CharSet.Auto)]
        public static extern IntPtr SendMessage(HandleRef hWnd, uint Msg, IntPtr wParam, IntPtr lParam);
    }    
}
