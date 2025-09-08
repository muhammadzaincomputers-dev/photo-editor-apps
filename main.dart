import 'package:flutter/material.dart';
import 'ui/editor_screen.dart';

void main() => runApp(PhotoEditorApp());

class PhotoEditorApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Photo Editor',
      home: EditorScreen(),
    );
  }
}
