package com.baidu.duer.files.filelist

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import com.baidu.duer.files.databinding.ItemDialogButtonGroupBinding
import com.baidu.duer.files.databinding.ItemDialogTitleBinding
import com.baidu.duer.files.databinding.NameDialogBinding
import com.baidu.duer.files.databinding.NameDialogNameIncludeBinding
import com.baidu.duer.files.dialog.BaseDialogFragment
import com.baidu.duer.files.util.*
import com.google.android.material.textfield.TextInputLayout

abstract class NameDialogFragment : BaseDialogFragment() {
    private lateinit var _binding: Binding
    protected open val binding: Binding
        get() = _binding

    protected open val listener: Listener
        get() = requireParentFragment() as Listener

    protected lateinit var baseDialog: Dialog

    override val width: Int
        get() = dpToDimensionPixelOffset(404)

    override val radius: Float
        get() = 12f

    override val y: Int
        get() = (-12).px

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = onInflateBinding(requireContext().layoutInflater)
        dialog?.apply {
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            setCancelable(true)
            setCanceledOnTouchOutside(onTouchOutsideIsCancel())
            setOnShowListener {
                binding.confirmButton.setOnClickListener { onOk() }
                binding.cancelButton.setOnClickListener { onCancel() }
                binding.nameEdit?.let { it1 -> showKeyboard(it1) }
            }
        }
        super.onCreateView(inflater, container, savedInstanceState)
        return binding.root
    }

    private fun showKeyboard(editText: EditText) {
        editText.post {
            val inputManager: InputMethodManager =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.showSoftInput(editText, 0)
        }
    }

    override fun initData() {
        val initialName = initialName
        if (initialName != null) {
            binding.nameEdit?.setText(initialName)
            binding.nameEdit?.setSelection(0, initialName.length)
        }
        binding.nameLayout?.let { binding.nameEdit?.hideTextInputLayoutErrorOnTextChange(it) }
        binding.nameEdit?.setOnEditorConfirmActionListener { onOk() }
        binding.dialogTitle.text = getString(titleRes, 0)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        baseDialog = super.onCreateDialog(savedInstanceState)
        return baseDialog
    }

    @get:StringRes
    protected abstract val titleRes: Int

    protected open val initialName: String? = null

    protected open fun onInflateBinding(inflater: LayoutInflater): Binding =
        Binding.inflate(inflater)

    protected open fun onTouchOutsideIsCancel() = true

    protected open val permitCheckNameIsValid = true

    private fun onOk() {
        val name = name
        binding.nameEdit?.let {
            if (!isNameValid(name)) {
                return
            }
        }
        onOk(name)
    }

    protected open fun onCancel() {
        dismiss()
    }

    protected open val name: String
        get() = binding.nameEdit?.text.toString().trim()

    protected open fun isNameValid(name: String): Boolean {
        if (name == initialName && permitCheckNameIsValid) {
            dismiss()
            return false
        }
        return true
    }

    protected open fun onOk(name: String) {
        dismiss()
    }

    protected open class Binding protected constructor(
        val root: View,
        val groupButton: LinearLayout,
        val cancelButton: TextView,
        val confirmButton: TextView,
        val dialogTitle: TextView,
        val nameLayout: TextInputLayout? = null,
        val nameEdit: EditText? = null,
    ) {
        companion object {
            fun inflate(inflater: LayoutInflater): Binding {
                val binding = NameDialogBinding.inflate(inflater)
                val bindingRoot = binding.root
                val itemDialogButtonGroup = ItemDialogButtonGroupBinding.bind(bindingRoot)
                val itemTitleBinding = ItemDialogTitleBinding.bind(bindingRoot)
                val nameBinding = NameDialogNameIncludeBinding.bind(bindingRoot)
                return Binding(
                    bindingRoot,
                    itemDialogButtonGroup.buttonGroup,
                    itemDialogButtonGroup.buttonCancel,
                    itemDialogButtonGroup.buttonConfirm,
                    itemTitleBinding.dialogTitle,
                    nameBinding.nameLayout,
                    nameBinding.nameEdit,
                )
            }
        }
    }

    interface Listener
}
