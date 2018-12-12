package pt.uc.cm.daylistudent.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_select_group.*
import pt.uc.cm.daylistudent.R
import pt.uc.cm.daylistudent.models.Group
import pt.uc.cm.daylistudent.utils.RetrofitUtils
import pt.uc.cm.daylistudent.utils.SharedPreferencesUtils
import pt.uc.cm.daylistudent.utils.SnackBarUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SelectGroupActivity : AppCompatActivity() {

    private lateinit var retrofit: RetrofitUtils
    private lateinit var selectedGroup: Group
    private var groups = mutableListOf<Group>()

    companion object {
        val TAG: String = SelectGroupActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(SharedPreferencesUtils.readTheme(applicationContext))
        setContentView(R.layout.activity_select_group)
        title = getString(R.string.Groups)

        retrofit = RetrofitUtils()

        val groupId = SharedPreferencesUtils.readSelectedGroupId()
        if (groupId != SharedPreferencesUtils.NONE_GROUP_ID) {
            startActivity(Intent(this, GlobalNotesActivity::class.java))
            finish()
        }

        getGroupsData()
    }

    private fun getGroupsData() {
        retrofit.getGroups(object : Callback<List<Group>> {
            override fun onFailure(call: Call<List<Group>>, t: Throwable) {
                Toast.makeText(applicationContext, getText(R.string.GetGroupsError), Toast.LENGTH_LONG).show()
                finish()
            }

            override fun onResponse(call: Call<List<Group>>, response: Response<List<Group>>) {
                response.body()!!.forEach {
                    groups.add(it)
                }
                fillSpinnerData()
            }
        })
    }

    private fun fillSpinnerData() {
        val groupNames = mutableListOf<String>()
        groups.forEach {
            groupNames.add(it.name!!)
        }

        val spinnerArrayAdapter: ArrayAdapter<String> = ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, groupNames)
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGroup.adapter = spinnerArrayAdapter

        spinnerGroup.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                btEnterGroup.isEnabled = false
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                btEnterGroup.isEnabled = true
                selectedGroup = groups[position]
            }
        }
    }

    fun onEnterGroupClick(view: View) {
        SharedPreferencesUtils.writeSelectedGroupId(applicationContext, selectedGroup.id)
        finish()
        startActivity(Intent(this, GlobalNotesActivity::class.java))
    }

    fun onCreateNewGroupClick(view: View) {
        val groupName = etNewGroupName.text.toString()
        retrofit.postGroup(groupName, object : Callback<Int> {
            override fun onFailure(call: Call<Int>, t: Throwable) {
                SnackBarUtil.showSnackBar(window.decorView.rootView, R.string.ExitGroup, true)
            }

            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                Toast.makeText(applicationContext, getString(R.string.CreateNewGroupSuccess),
                        Toast.LENGTH_LONG).show()
                val groupId = response.body()
                SharedPreferencesUtils.writeSelectedGroupId(applicationContext, groupId)
                finish()
                startActivity(Intent(applicationContext, GlobalNotesActivity::class.java))
            }
        })
    }
}
