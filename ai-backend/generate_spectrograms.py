import os
import librosa
import librosa.display
import numpy as np
import matplotlib.pyplot as plt
from tqdm import tqdm

DATASET_PATH = r"D:\AuraShield_AI\dataset\selected"

OUTPUT_PATH = r"D:\AuraShield_AI\ai-backend\spectrograms"


def create_spectrogram(input_file, output_file):

    try:

        audio, sr = librosa.load(
            input_file,
            sr=16000,
            mono=True
        )

        mel = librosa.feature.melspectrogram(
            y=audio,
            sr=sr,
            n_mels=128
        )

        mel_db = librosa.power_to_db(
            mel,
            ref=np.max
        )

        plt.figure(figsize=(3, 3))

        librosa.display.specshow(
            mel_db,
            sr=sr
        )

        plt.axis("off")

        plt.savefig(
            output_file,
            bbox_inches="tight",
            pad_inches=0
        )

        plt.close()

    except Exception as e:

        print("ERROR:", input_file)
        print(e)


for label in ["fake", "real"]:

    input_folder = os.path.join(
        DATASET_PATH,
        label
    )

    output_folder = os.path.join(
        OUTPUT_PATH,
        label
    )

    files = [
        f for f in os.listdir(input_folder)
        if f.endswith(".wav")
    ]

    print(f"\nProcessing {label}...")

    for file in tqdm(files):

        input_file = os.path.join(
            input_folder,
            file
        )

        output_file = os.path.join(
            output_folder,
            file.replace(".wav", ".png")
        )

        create_spectrogram(
            input_file,
            output_file
        )

print("\nDONE")