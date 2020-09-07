from flask import Flask, request, jsonify
import json, base64, os

app = Flask(__name__)


@app.route('/syncImage', methods=['GET', 'POST'])
def add_message():
    data = request.json
    status = processingData(data)
    return jsonify(status)

def processingData(data):
    path_save = 'DataCheckIn/2020'
    data_imgs = data['data_imgs']
    path_date = data['path_date'].split('/')
    classID = path_date[-2]
    dateImg = path_date[-1]
    try:
        path_save = os.path.join(path_save,classID,dateImg)
        os.makedirs(path_save,exist_ok=False)
        for img in data_imgs:
            name_img = img['image_name']
            base64_image = img['base64_image']
            imgdata = base64.b64decode(base64_image)
            filename = os.path.join(path_save,name_img)  # I assume you have a way of picking unique filenames
            with open(filename, 'wb') as f:
                f.write(imgdata)

        return 'OK'
    except Exception as e:
        return e.message


if __name__ == '__main__':
    app.run(host='192.168.20.163', port=5001, debug=True)
