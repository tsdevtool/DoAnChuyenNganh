import 'dart:math';

import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:ungdungdatlichkham/providers/chat_provider.dart';
import 'package:ungdungdatlichkham/utility/utilites.dart';
import 'package:ungdungdatlichkham/widgets/preview_images_widget.dart';
class BottomChatField extends StatefulWidget {
  const BottomChatField({super.key, required this.chatProvider,});

  final ChatProvider chatProvider;
  @override
  State<BottomChatField> createState() => _BottomChatFieldState();
}

class _BottomChatFieldState extends State<BottomChatField> {
// controller for the input field
  final TextEditingController textController = TextEditingController();
  // focus node for the input field
  final FocusNode textFieldFocus = FocusNode();

  // inittialzie image picker
  final ImagePicker _picker = ImagePicker();
  @override
  void dispose() {
   textController.dispose();
   textFieldFocus.dispose();
    super.dispose();
  }
  Future<void> sendChatMessage({
    required String message,
    required ChatProvider chatProvider,
    required bool isTextOnly,
}) async {
    try{
      await chatProvider.sentMessage(
          message: message,
          isTextOnly: isTextOnly,);
    }catch(e){
      log('error: $e' as num);
    }finally{
      textController.clear();
      widget.chatProvider.setImagesFileList(listValue: []);
      textFieldFocus.unfocus();
    }
  }

  // pick an image
  void pickImage() async{
    try{
      final pickedImages = await _picker.pickMultiImage(
        maxHeight: 800,
        maxWidth: 800,
        imageQuality: 95,
      );
      widget.chatProvider.setImagesFileList(listValue: pickedImages);
    }catch(e){
      log('error : $e' as num);
    }

  }

  @override
  Widget build(BuildContext context) {
    bool HasImages = widget.chatProvider.imagesFileList!= null &&
          widget.chatProvider.imagesFileList!.isNotEmpty;
    return Container(
      decoration: BoxDecoration(
        color: Theme.of(context).cardColor,
        borderRadius: BorderRadius.circular(30),
        border: Border.all(
          color: Theme.of(context).textTheme.titleLarge!.color!,
        )
      ) ,
          child:Column(
            children: [
              if(HasImages) const PreviewImagesWidget(),
              Row(
                children: [
                  IconButton(
                      onPressed: (){
                        if(HasImages){
                          //show the delete dialog
                          showMyAnimatedDialog(
                              context: context,
                              title: 'Delete Images',
                              content: 'Are you sure you want o delete the image?',
                              actionText: 'Delete',
                              onActionPressed: (value){
                                if(value){
                                  widget.chatProvider.setImagesFileList(listValue: []);
                                }
                              });
                        }else{
                          pickImage();
                        }
                      },

                      icon: HasImages?  Icon(Icons.delete_forever_outlined, color: Colors.red,size: 30,) : Icon(Icons.image_outlined, color: Color.fromARGB(255, 47, 100, 253), size: 30,)),
                      const SizedBox(width: 5,),
                  Expanded(
                      child: TextField(
                        focusNode: textFieldFocus,
                        controller: textController,
                        textInputAction: TextInputAction.send,
                        onSubmitted:  widget.chatProvider.isLoading? null : (String value){
                               if(value.isNotEmpty) {
                                 //send the message
                                 sendChatMessage(
                                   message: textController.text,
                                   chatProvider: widget.chatProvider,
                                   isTextOnly: HasImages? false : true,
                                 );
                               }
                        },
                        decoration: InputDecoration.collapsed(
                          hintText: 'Nhập câu hỏi mà bạn muốn hỏi...',
                          hintStyle:TextStyle(
                              color: Colors.grey,
                              fontSize: 17
                          ),
                          border: OutlineInputBorder(
                            borderSide: BorderSide.none,
                            borderRadius: BorderRadius.circular(30),
                          ),
                        ),
                      )
                  ),
                   GestureDetector(
                     onTap: widget.chatProvider.isLoading ? null :(){
                       if(textController.text.isNotEmpty){
                         //send the message
                         sendChatMessage(
                           message: textController.text,
                           chatProvider: widget.chatProvider,
                           isTextOnly: HasImages? false : true,);
                       }

                     },
                     child: Container(
                         decoration: BoxDecoration(
                           color: Color.fromARGB(255, 47, 100, 253),
                           borderRadius: BorderRadius.circular(20),
                         ),
                         margin: const EdgeInsets.all(5.0),
                         child: const Padding(
                           padding: EdgeInsets.all(8.0),
                           child: Icon(Icons.send,color: Colors.white),
                         )),
                   )
                ],
              ),
            ],
          ) ,
    );
  }
}
